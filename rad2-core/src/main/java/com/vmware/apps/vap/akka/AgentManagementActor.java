package com.vmware.apps.vap.akka;

import akka.actor.Props;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.vmware.akka.common.BaseVapActorWithTimer;
import com.vmware.common.constants.VapServiceConstants;
import com.vmware.common.metrics.MetricsStreamParser;
import com.vmware.common.utils.PrintUtils;
import com.vmware.ignite.common.RegistryManager;
import com.vmware.ignite.util.JobType;
import com.vmware.apps.vap.ignite.EndpointRegistry;
import com.vmware.vap.bridge.common.VAPBridgeConstants;
import com.vmware.vap.service.VapServiceUtils;
import com.vmware.vap.service.control.OnPremVapDelegate;
import com.vmware.vap.service.dto.*;
import com.vmware.vap.service.exception.LemansServiceException;
import com.vmware.vap.service.message.ActorMessage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.vmware.common.constants.VapServiceConstants.*;

public class AgentManagementActor extends BaseVapActorWithTimer {
    private OnPremVapDelegate onPremVapDelegate;
    private static Logger logger = LoggerFactory.getLogger(AgentManagementActor.class);

    private AgentManagementActor(RegistryManager rm, OnPremVapDelegate onPremVapDelegate) {
        super(rm);
        this.onPremVapDelegate = onPremVapDelegate;
    }

    public static Props props(RegistryManager rm, OnPremVapDelegate onPremVapDelegate) {
        return Props.create(AgentManagementActor.class, rm, onPremVapDelegate);
    }

    @Override
    public Receive createReceive() {
        return super.createReceive().orElse(receiveBuilder()
            .match(Meps.class, this::handleEndpointStateData)
            .match(AgentManagement.class, this::manageAgent)
            .match(HandleAgtMgmtCallBackMsg.class, this::handleAgentMgmtCallback).build());
    }

    @Override
    protected void checkJobStatus(JobStatusTick jobStatusTick) {
        UUID requestId = jobStatusTick.getKey().getRequestID();
        String agentId = jobStatusTick.getKey().getCloudProxyID();

        try {
            AgentManagementResponse agentManagementResponse = onPremVapDelegate
                .checkJobStatus(requestId, agentId, JobType.AGENT_MANAGEMENT);

            updateJob(jobStatusTick,
                JobType.AGENT_MANAGEMENT,
                agentManagementResponse.taskInfo.stage.toString());
        } catch (LemansServiceException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    private void handleEndpointStateData(Meps mepsData) {
        String payload = mepsData.getPayload();
        EndpointRegistry endpointRegistry = reg(EndpointRegistry.class);
        Map<String, Map<String, Object>> endpointStatus = new HashMap<>();

        List<MetricsStreamParser.MetricLineParts> metricLineParts = MetricsStreamParser.parsePayload(payload);
        for (final MetricsStreamParser.MetricLineParts metricLinePart : metricLineParts) {
            if (null != metricLinePart.getMetricName() && !metricLinePart.getMetricName().endsWith(".")) {
                String suffix = metricLinePart.getMetricName().substring(
                    metricLinePart.getMetricName().lastIndexOf(".") + 1);
                if (StringUtils.equals(suffix, VapServiceConstants.STATUS) || StringUtils.equals(suffix,
                    VapServiceConstants.BOOTSTRAP_STATUS)) {
                    final Map<String, String> pointTags = metricLinePart.getKeyValuePairs();
                    final String id = VapServiceUtils.contructEndpointID(metricLinePart.getVc_uuid(),
                        metricLinePart.getVm_mor());
                    if (!endpointStatus.containsKey(id)) {
                        endpointStatus.put(id, new HashMap<>());
                    }

                    final Map<String, Object> endpointState = endpointStatus.get(id);
                    if (pointTags.containsKey(VapServiceConstants.SERVICE)) {
                        endpointState.put(pointTags.get(VapServiceConstants.SERVICE),
                            metricLinePart.getMetricValue()
                                == 1 ? EndpointDTO.AgentState.RUNNING : EndpointDTO.AgentState.STOPPED);
                    } else if (pointTags.containsKey(VapServiceConstants.CONTENT_VERSION)) {
                        endpointState.put(VapServiceConstants.CONTENT_VERSION,
                            pointTags.get(VapServiceConstants.CONTENT_VERSION));
                    }
                }
            }
        }

        for (final String id : endpointStatus.keySet()) {
            final Map<String, Object> value = endpointStatus.get(id);
            endpointRegistry.updateAgentStatus(id,
                VapServiceUtils.convertToJson(value, new TypeToken<Map<String, Object>>() {
                }.getType()));
        }
    }

    private void manageAgent(AgentManagement agentManagement) {
        try {
            onPremVapDelegate.manageAgent(agentManagement.requestID, agentManagement.agentManagementDTO);
            List<String> listOfEndpoints = generateListOfEndpoints(agentManagement.agentManagementDTO
                .getAgentManagement().getEndpoints());

            JobStatusTick jobStatusTick =
                initializeJobWithPeriodicCheck(agentManagement.agentManagementDTO.getCloudProxyId(),
                    agentManagement.requestID,
                    JobType.AGENT_MANAGEMENT,
                    agentManagement.getJobData(), 60, listOfEndpoints);
            jobTicksForReqMap.put(agentManagement.requestID, jobStatusTick);
        } catch (LemansServiceException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    /**
     * This method handles all callbacks w.r.t Agentmanagement start/stop actions
     */
    private void handleAgentMgmtCallback(HandleAgtMgmtCallBackMsg message) {
        JsonObject serviceResponse = new Gson()
            .fromJson(message.getPayload(), JsonObject.class);
        if (!checkIfResponseIsValid(serviceResponse)) {
            PrintUtils.printToActor(String.format(
                "Did not receive a valid callback response for agent "
                    + "management %s",
                message.getPayload()));
            return;
        }

        UUID serviceId =
            UUID.fromString(serviceResponse.get(REQUEST_ID).getAsString());
        String status = serviceResponse.get(STATUS).getAsString();
        String endpointId =
            serviceResponse.get(VAPBridgeConstants.VM_ID).getAsString();

        if (jobTicksForReqMap.containsKey(serviceId)) {
            JobStatusTick jobStatusTick = jobTicksForReqMap.get(serviceId);

            Map<String, String> endpointStatus = new HashMap<>();
            endpointStatus.put(endpointId, status);

            updateJobWithEndpointStatus(jobStatusTick,
                JobType.AGENT_MANAGEMENT,
                IN_PROGRESS,
                endpointStatus);

            jobTicksForReqMap.remove(serviceId);
        }
    }

    private boolean checkIfResponseIsValid(JsonObject serviceResponse) {

        return (serviceResponse.get(VAPBridgeConstants.VM_ID) != null
            && serviceResponse.get(AGENT_NAME) != null
            && serviceResponse.get(STATUS) != null
            && serviceResponse.get(VAPBridgeConstants.REQUEST_ID) != null);
    }

    private List<String> generateListOfEndpoints(
        List<EndpointSearchItemDTO> endpoints) {
        List<String> endpointsList = new ArrayList();
        for (EndpointSearchItemDTO endpoint : endpoints) {
            for (String vmMor : endpoint.getVmMors()) {
                endpointsList.add(VapServiceUtils.contructEndpointID(endpoint.getVcUUID(),
                    vmMor));
            }
        }
        return endpointsList;
    }

    public static class Meps extends ActorMessage {
        public Meps(CallbackMessageDTO callbackMessageDTO) {
            super(callbackMessageDTO);
        }
    }

    public static class HandleAgtMgmtCallBackMsg extends ActorMessage {
        public HandleAgtMgmtCallBackMsg(CallbackMessageDTO messageDTO) {
            super(messageDTO);
        }
    }

    public static class AgentManagement {
        private UUID requestID;
        private AgentManagementRequest agentManagementDTO;

        public AgentManagement(UUID requestID, AgentManagementRequest agentManagementDTO) {
            this.requestID = requestID;
            this.agentManagementDTO = agentManagementDTO;
        }

        public String getJobData() {
            return agentManagementDTO.getJobData();
        }
    }
}
