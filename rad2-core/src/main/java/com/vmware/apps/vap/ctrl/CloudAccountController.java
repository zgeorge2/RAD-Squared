package com.vmware.apps.vap.ctrl;

import com.vmware.akka.common.AkkaAskAndWait;
import com.vmware.apps.vap.akka.CloudAccountActor;
import com.vmware.common.constants.VapServiceConstants;
import com.vmware.vap.service.control.VapActorType;
import com.vmware.vap.service.dto.CloudAccountToVAPMappingRequest;
import com.vmware.vap.service.dto.ValidateCloudAccountRequest;
import com.vmware.vap.service.dto.ValidateCloudAccountResponse;

import java.util.List;
import java.util.UUID;

public class CloudAccountController extends BaseVAPController {
    @Override
    protected VapActorType getActorType() {
        return VapActorType.CLOUD_ACCOUNT;
    }

    public ValidateCloudAccountResponse validateCloudAccount(ValidateCloudAccountRequest accountDTO) {
        UUID requestId = getUUIDGen().generateNewUUID();
        AkkaAskAndWait<CloudAccountActor.ValidateCloudAccountMessage, ValidateCloudAccountResponse> askAndWait = new AkkaAskAndWait<>(getActor());
        return askAndWait.askAndWait(new CloudAccountActor.ValidateCloudAccountMessage(requestId,
            accountDTO), VapServiceConstants.LEMANS_SYNC_CMD_TIMEOUT_SECS);
    }

    public boolean mapCloudAccount(CloudAccountToVAPMappingRequest vapToCloudAccountMapperRequest) {
        AkkaAskAndWait<CloudAccountActor.MapCloudAccountToVAPMessage, Boolean> askAndWait =
            new AkkaAskAndWait<>(getActor());

        return askAndWait.askAndWait(new CloudAccountActor.MapCloudAccountToVAPMessage(vapToCloudAccountMapperRequest));
    }

    public List<CloudAccountToVAPMappingRequest> getAll() {
        AkkaAskAndWait<CloudAccountActor.GetAllCloudAccountMessage,
            List<CloudAccountToVAPMappingRequest>> askAndWait =
            new AkkaAskAndWait<>(getActor());
        return askAndWait.askAndWait(new CloudAccountActor.GetAllCloudAccountMessage());
    }

    public CloudAccountToVAPMappingRequest get(String vapDcId) {
        AkkaAskAndWait<CloudAccountActor.GetCloudAccountMessage,
            CloudAccountToVAPMappingRequest> askAndWait =
            new AkkaAskAndWait<>(getActor());
        return askAndWait.askAndWait(new CloudAccountActor.GetCloudAccountMessage(vapDcId));
    }
}
