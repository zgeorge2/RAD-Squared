package com.vmware.apps.vap.akka;

import akka.actor.Props;
import com.vmware.akka.common.BaseActor;
import com.vmware.akka.common.BaseVapActorWithTimer;
import com.vmware.akka.common.RegistryStateDTO;
import com.vmware.ignite.common.RegistryManager;
import com.vmware.ignite.util.JobType;
import com.vmware.apps.vap.ignite.JobRegistry;
import com.vmware.vap.service.control.LemansOnPremVapDelegate;
import com.vmware.vap.service.control.OnPremVapDelegate;
import com.vmware.vap.service.dto.CloudAccountToVAPMappingRequest;
import com.vmware.vap.service.dto.ValidateCloudAccountRequest;
import com.vmware.vap.service.dto.ValidateCloudAccountResponse;
import com.vmware.vap.service.exception.LemansServiceException;
import com.vmware.vap.service.model.VapDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cloud Account Management Actor. One of the cloud account management is to validate the cloud account before
 * persisting.
 */
public class CloudAccountActor extends BaseActor {
    private static Logger logger = LoggerFactory.getLogger(CloudAccountActor.class);
    private OnPremVapDelegate onPremVapDelegate;

    private CloudAccountActor(RegistryManager rm) {
        super(rm);
        this.onPremVapDelegate = new LemansOnPremVapDelegate(this.getRM());
    }

    public OnPremVapDelegate getOnPremVapDelegate() {
        return onPremVapDelegate;
    }

    public static Props props(RegistryManager rm) {
        return Props.create(CloudAccountActor.class, rm);
    }

    @Override
    public Receive createReceive() {
        return super.createReceive().orElse(receiveBuilder().
            match(ValidateCloudAccountMessage.class, this::handleCloudAccountValidation).
            match(MapCloudAccountToVAPMessage.class, this::mapCloudAccountToVap).
            match(GetAllCloudAccountMessage.class, this::getAllCloudAccountWithMappedVap).
            match(GetCloudAccountMessage.class, this::getCloudAccount).
            build());
    }

    private void getAllCloudAccountWithMappedVap(GetAllCloudAccountMessage getAllCloudAccountMessage) {
        this.sender().tell(CloudAccountStore.getAll(), this.self());
    }

    private void getCloudAccount(GetCloudAccountMessage getCloudAccountMessage) {
        this.sender().tell(CloudAccountStore.get(getCloudAccountMessage.getVapDcId()), this.self());
    }

    private void mapCloudAccountToVap(MapCloudAccountToVAPMessage mapCloudAccountMessage) {
        this.sender().tell(CloudAccountStore.map(mapCloudAccountMessage.getVAPToCloudAccountMapper()),
            this.self());
    }

    private void handleCloudAccountValidation(ValidateCloudAccountMessage message) {
        VapDTO vapDTO = new VapDTO("vap-name", message.getValidateCloudAccountRequest().getLemansAgentId(),
            message
                .getValidateCloudAccountRequest().getCloudProxyId());
        // Update Job DTO
        JobRegistry jobReg = reg(JobRegistry.class);
        RegistryStateDTO dto = new BaseVapActorWithTimer.JobStatusRegistryStateDTO(vapDTO.getAgentId(),
            message.getRequestId().toString(), JobType.CLOUD_ACCOUNT_VALIDATION, JobRegistry.Job
            .JOB_STATUS_STARTING, "", System.currentTimeMillis(), "");
        jobReg.add(dto);
        jobReg.updateJobStatus(message.requestId.toString(), vapDTO.getAgentId(),
            JobRegistry.Job.JOB_STATUS_STARTING);
        ValidateCloudAccountResponse response = null;
        String statusMesg = "";
        try {
            response = getOnPremVapDelegate().validateCloudAccount(message.getRequestId(),
                message.getValidateCloudAccountRequest());
        } catch (LemansServiceException e) {
            logger.error(e.getMessage() + " - " + e.getCause().getMessage(), e);
            statusMesg = e.getCause().getMessage();
        } finally {
            if (response == null) {
                response = new ValidateCloudAccountResponse(message.getValidateCloudAccountRequest(), "ERROR",
                    statusMesg);
            }
            jobReg.updateJobStatus(vapDTO.getAgentId(), message.getRequestId().toString(),
                response.getStatus(),
                response.getStatusMesg());
            sender().tell(response, this.getSelf());
        }
    }

    public static class ValidateCloudAccountMessage {
        UUID requestId;
        ValidateCloudAccountRequest validateCloudAccountRequest;

        public ValidateCloudAccountMessage(UUID requestId, ValidateCloudAccountRequest accountDTO) {
            this.requestId = requestId;
            this.validateCloudAccountRequest = accountDTO;
        }

        public UUID getRequestId() {
            return requestId;
        }

        public ValidateCloudAccountRequest getValidateCloudAccountRequest() {
            return validateCloudAccountRequest;
        }
    }

    public static class GetAllCloudAccountMessage {
    }

    public static class GetCloudAccountMessage {
        String vapDcId;

        public GetCloudAccountMessage(String cloudAccountName) {
            this.vapDcId = cloudAccountName;
        }

        public String getVapDcId() {
            return vapDcId;
        }
    }

    public static class MapCloudAccountToVAPMessage {
        CloudAccountToVAPMappingRequest VAPToCloudAccountMapper;

        public MapCloudAccountToVAPMessage(CloudAccountToVAPMappingRequest VAPToCloudAccountMapper) {
            this.VAPToCloudAccountMapper = VAPToCloudAccountMapper;
        }

        public CloudAccountToVAPMappingRequest getVAPToCloudAccountMapper() {
            return VAPToCloudAccountMapper;
        }
    }

    //TODO: we need to remove this store once the discovery service patching mechanism is established.
    public static class CloudAccountStore {
        /**
         * CloudAccountId to Vap ID map. one vap instance can be associated with multiple cloud accounts. And
         * there can be many vap instances
         */
        public static Map<String, CloudAccountToVAPMappingRequest> cloudAccountIdToVap =
            new ConcurrentHashMap<>();

        public static boolean map(CloudAccountToVAPMappingRequest vapToCloudAccountMapper) {
            cloudAccountIdToVap.put(vapToCloudAccountMapper.cloudAccountId, vapToCloudAccountMapper);
            return true;
        }

        public static List<CloudAccountToVAPMappingRequest> getAll() {
            return new ArrayList<>(cloudAccountIdToVap.values());
        }

        public static CloudAccountToVAPMappingRequest get(String cloudAccountId) {
            return cloudAccountIdToVap.get(cloudAccountId);
        }
    }
}
