package com.vmware.apps.vap.akka;

import akka.actor.Props;
import com.google.gson.Gson;
import com.vmware.akka.common.BaseVapActorWithTimer;
import com.vmware.akka.common.RegistryStateDTO;
import com.vmware.common.HasEndpointIdentifier;
import com.vmware.common.constants.VapServiceConstants;
import com.vmware.common.utils.PrintUtils;
import com.vmware.common.validation.ValidCloudAccount;
import com.vmware.ignite.common.DModel;
import com.vmware.ignite.common.RegistryManager;
import com.vmware.ignite.util.JobType;
import com.vmware.apps.vap.ignite.EndpointRegistry;
import com.vmware.apps.vap.ignite.JobRegistry;
import com.vmware.vap.service.VapServiceUtils;
import com.vmware.vap.service.control.OnPremVapDelegate;
import com.vmware.vap.service.dto.AgentDeploymentRequest;
import com.vmware.vap.service.dto.AgentDeploymentResponse;
import com.vmware.vap.service.dto.CallbackMessageDTO;
import com.vmware.vap.service.exception.LemansServiceException;
import com.vmware.vap.service.message.ActorMessage;
import com.vmware.xenon.common.TaskState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * The Actor that performs operations on a VAP Instance onPrem.
 */
public class BootstrapActor extends BaseVapActorWithTimer {
    private OnPremVapDelegate onPremVapDelegate;
    private static Logger logger = LoggerFactory.getLogger(BootstrapActor.class);

    private BootstrapActor(RegistryManager rm, OnPremVapDelegate onPremVapDelegate) {
        super(rm);
        this.onPremVapDelegate = onPremVapDelegate;
    }

    static public Props props(RegistryManager rm, OnPremVapDelegate onPremVapDelegate) {
        return Props.create(BootstrapActor.class, rm, onPremVapDelegate);
    }

    @Override
    public Receive createReceive() {
        return super.createReceive().orElse(
                    receiveBuilder().match(BootstrapEndpoint.class, this::bootstrapEndpoint).match(
                                HandleBootstrapMessage.class, this::handleBootstrapEndpointCallbackMessage).build());
    }

    private void handleBootstrapEndpointCallbackMessage(HandleBootstrapMessage messageBody) {
        EndpointRegistry epReg = (EndpointRegistry) getRM()
          .get(EndpointRegistry.class);
        JobRegistry jobReg = (JobRegistry) getRM().get(JobRegistry.class);

        // TODO: Assuming there is only one json per payload and messageTopic is passed as part of the message for time
        // being till we finalize on the message fields
        Gson gson = new Gson();
        HandleBootstrapMessage.BootstrapMessage message = gson.fromJson(
          messageBody.getPayload(),
          HandleBootstrapMessage.BootstrapMessage.class);

        if (!validator.isValid(message, messageBody.getAgentId())) {
            //TODO: Handle Invalid payload for agent
        }

        String endpointID = VapServiceUtils
          .contructEndpointID(message.getVc_uuid(), message.getVm_mor());
        int currentStage = message.getCurrentstage();
        int totalStages = message.getTotalstages();
        UUID requestId = UUID.fromString(message.getRequestId());
        PrintUtils.printToActor(
          endpointID + "--------" + message.getStatus() + " --- " + currentStage
            + "/" + totalStages);
        if (jobTicksForReqMap.containsKey(requestId)) {

            String jobKey = jobReg.getJobKey(requestId.toString(),
              JobType.AGENT_DEPLOYMENT.getName());
            JobRegistry.Job job = jobReg.get(jobKey);

            epReg.update(endpointID,
              job.getJobType() + ":" + message.getStatus() + ":" + currentStage,
              message.getRequestId());

            HashMap<String, String> endpointMap = new HashMap<>();
            endpointMap
              .put(endpointID, message.getStatus() + ":" + currentStage);
            this.updateJobWithEndpointStatus(jobTicksForReqMap.get(requestId),
              JobType.AGENT_DEPLOYMENT,
              VapServiceConstants.IN_PROGRESS,
              endpointMap);
        }
    }

    private OnPremVapDelegate getOnPremVapDelegate() {
        return this.onPremVapDelegate;
    }

    private void bootstrapEndpoint(BootstrapEndpoint request) {

        EndpointRegistry endpointRegistry = reg(EndpointRegistry.class);
        List<String> endpointsList = new ArrayList<>();

        // Iterate through all the endpoints and create the entries for them
        for (final AgentDeploymentRequest.ManageEndpointsDTO agentManagementDTO : request.agentDetails.getManageEndpointServiceStates()) {
            for (final AgentDeploymentRequest.EndPointDTO endPointDTO : agentManagementDTO.getEndpoints()) {
                EndPointRegistryStateDTO epDTO = new EndPointRegistryStateDTO(endPointDTO);
                endpointRegistry.add(epDTO);
                endpointsList.add(VapServiceUtils.contructEndpointID(endPointDTO.getVc_id(), endPointDTO.getVm_mor()));
            }
        }

        try {
            getOnPremVapDelegate().bootstrapEndpoint(request.requestID, request.agentDetails);
            JobStatusTick jobStatusTick = initializeJobWithPeriodicCheck(
              request.agentDetails.getCloudProxyId(),
              request.requestID,
              JobType.AGENT_DEPLOYMENT,
              request.getJobData(),
              600,
              endpointsList);
            jobTicksForReqMap.put(request.requestID, jobStatusTick);
        }
        catch(LemansServiceException ex){
            logger.error(ex.getMessage(), ex);
        }
    }

    @Override
    protected void checkJobStatus(JobStatusTick jobStatusTick) {
        UUID requestId = jobStatusTick.getKey().getRequestID();
        String agentId = jobStatusTick.getKey().getCloudProxyID();

        EndpointRegistry epReg = reg(EndpointRegistry.class);
        try {
            AgentDeploymentResponse agentDeploymentResponse = getOnPremVapDelegate()
              .checkJobStatus(requestId, agentId, JobType.AGENT_DEPLOYMENT);

            if (agentDeploymentResponse.getJob() != null) {
                String job = agentDeploymentResponse.getJob().name();
                agentDeploymentResponse.getVmStatus().forEach((vmId, status) -> {
                    PrintUtils.printToActor(vmId + "--------" + status);
                    epReg.update(vmId, job + "_" + status, requestId.toString());
                });
            }
            TaskState.TaskStage bootstrapStage = agentDeploymentResponse.taskInfo.stage;
            updateJob(jobStatusTick, JobType.AGENT_DEPLOYMENT, bootstrapStage.toString());
            if(bootstrapStage.equals(agentDeploymentResponse.taskInfo.stage.FINISHED)){
                jobTicksForReqMap.remove(requestId);
            }
        }
        catch (LemansServiceException ex){
            logger.error(ex.getMessage(), ex);
        }

    }

    public static class HandleBootstrapMessage extends ActorMessage {
        public HandleBootstrapMessage(CallbackMessageDTO messageDTO) {
            super(messageDTO);
        }

        public static class BootstrapMessage implements HasEndpointIdentifier {
            float timestamp;
            // TODO: Assuming topic name is sent in the message
            String messageTopic;
            String progressText;
            String status;
            @ValidCloudAccount
            String vc_uuid;
            String vm_mor;
            String requestId;
            int currentstage;
            int totalstages;


            public BootstrapMessage() {
            }

            public float getTimestamp() {
                return timestamp;
            }

            public String getMessageTopic() {
                return messageTopic;
            }

            public String getProgressText() {
                return progressText;
            }

            public String getStatus() {
                return status;
            }

            public void setStatus(String status) {
                this.status = status;
            }

            @Override
            public String getVc_uuid() {
                return vc_uuid;
            }

            public void setVc_uuid(String vc_uuid) {
                this.vc_uuid = vc_uuid;
            }

            @Override
            public String getVm_mor() {
                return vm_mor;
            }

            public void setVm_mor(String vm_mor) {
                this.vm_mor = vm_mor;
            }

            public String getRequestId() {
                return requestId;
            }

            public void setRequestId(String requestId) {
                this.requestId = requestId;
            }

            public int getCurrentstage() {
                return currentstage;
            }

            public void setCurrentstage(int currentstage) {
                this.currentstage = currentstage;
            }

            public int getTotalstages() {
                return totalstages;
            }

            public void setTotalstages(int totalstages) {
                this.totalstages = totalstages;
            }
        }
    }

    public static class BootstrapEndpoint {
        private AgentDeploymentRequest agentDetails;
        private UUID requestID;

        public BootstrapEndpoint(AgentDeploymentRequest agentDetails, UUID requestID) {
            this.agentDetails = agentDetails;
            this.requestID = requestID;
        }

        public AgentDeploymentRequest getAgentDetails() {
            return agentDetails;
        }

        public UUID getRequestID() {
            return requestID;
        }

        public String getJobData() {
            return agentDetails.getJobData();
        }
    }


    public static class EndPointRegistryStateDTO extends RegistryStateDTO {
        private static final String ATTR_VC_ID_KEY = "VC_ID_KEY";
        private static final String ATTR_VM_MOR_KEY = "VM_MOR_KEY";
        private static final String ATTR_USER_KEY = "USER_KEY";
        private static final String ATTR_PASSWORD_KEY = "PASSWORD_KEY";
        private static final String ATTR_JOB_ID_KEY = "JOB_ID_KEY";

        public EndPointRegistryStateDTO(
                    String vc_id,
                    String vm_mor,
                    String user,
                    String password,
                    String jobId) {
            super(EndpointRegistry.class, vc_id, vm_mor); // parentKey = Vap; name = vmName
            this.putAttr(ATTR_VC_ID_KEY, vc_id);
            this.putAttr(ATTR_VM_MOR_KEY, vm_mor);
            this.putAttr(ATTR_USER_KEY, user);
            this.putAttr(ATTR_PASSWORD_KEY, password);
            this.putAttr(ATTR_JOB_ID_KEY, jobId);
        }

        public EndPointRegistryStateDTO(EndpointRegistry.Endpoint model) {
            super(EndpointRegistry.class, model);
            this.putAttr(ATTR_VC_ID_KEY, model.getVcUUID());
            this.putAttr(ATTR_VM_MOR_KEY, model.getVmMOR());
            this.putAttr(ATTR_USER_KEY, model.getUser());
            this.putAttr(ATTR_PASSWORD_KEY, model.getPassword());
            this.putAttr(ATTR_JOB_ID_KEY, model.getJobId());
        }

        public EndPointRegistryStateDTO(AgentDeploymentRequest.EndPointDTO reqDTO) {
            this(reqDTO.getVc_id(), reqDTO.getVm_mor(), reqDTO.getUser(),
                        reqDTO.getPassword(), reqDTO.getJobId());
        }

        @Override
        public DModel toModel() {
            return new EndpointRegistry.Endpoint(this);
        }

        @Override
        public String getKey() {
            return String.format("%s_%s", this.getVc_id(), this.getVm_mor());
        }

        public String getVc_id() {
            return (String) this.getAttr(ATTR_VC_ID_KEY);
        }

        public String getVm_mor() {
            return (String) this.getAttr(ATTR_VM_MOR_KEY);
        }

        public String getUser() {
            return (String) this.getAttr(ATTR_USER_KEY);
        }

        public String getPassword() {
            return (String) this.getAttr(ATTR_PASSWORD_KEY);
        }

        public String getVmName() {
            return this.getName();
        }

        public String getVapName() {
            return this.getParentKey();
        }

        public String getJobId() {
            return (String) this.getAttr(ATTR_JOB_ID_KEY);
        }
    }
}
