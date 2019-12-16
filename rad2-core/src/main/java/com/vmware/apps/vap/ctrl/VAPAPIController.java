package com.vmware.apps.vap.ctrl;

import akka.actor.ActorSelection;
import com.vmware.akka.common.AkkaAskAndWait;
import com.vmware.apps.vap.akka.VapRetrieverActor;
import com.vmware.common.constants.VapServiceConstants;
import com.vmware.common.utils.PrintUtils;
import com.vmware.vap.service.control.VapActorType;
import com.vmware.vap.service.dto.EndpointDTO;
import com.vmware.vap.service.dto.EndpointSearchRequestDTO;

import java.util.List;

public class VAPAPIController extends BaseVAPController {

    @Override
    protected VapActorType getActorType(){
        return VapActorType.RETRIEVER;
    }

    public List<EndpointDTO> getManagedEndpoints(EndpointSearchRequestDTO endpointSearchRequestDTO) {
        PrintUtils.printToActor("Recieved request to fetch endpoint details");
        // Pagination pagination = new Pagination(pageNo, pageSize);
        ActorSelection retriverActor = getActor();
        AkkaAskAndWait<VapRetrieverActor.EndpointRetreiver, List<EndpointDTO>> askAndWait =
                    new AkkaAskAndWait<>(retriverActor);

        List<EndpointDTO> endpointDTOs =
                    askAndWait.askAndWait(new VapRetrieverActor.EndpointRetreiver(endpointSearchRequestDTO),
                                VapServiceConstants.GET_VM_API_TIMEOUT_IN_SEC);
        return endpointDTOs;
    }


}
