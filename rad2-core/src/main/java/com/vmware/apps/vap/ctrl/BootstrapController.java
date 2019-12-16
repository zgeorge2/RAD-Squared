package com.vmware.apps.vap.ctrl;

import akka.actor.ActorRef;
import com.vmware.apps.vap.akka.BootstrapActor;
import com.vmware.common.utils.PrintUtils;
import com.vmware.vap.service.control.VapActorType;
import com.vmware.vap.service.dto.AgentDeploymentRequest;
import com.vmware.vap.service.dto.BootstrapEndpointResponse;

import java.util.UUID;

public class BootstrapController extends BaseVAPController {

    @Override
    protected VapActorType getActorType() {
        return VapActorType.BOOTSTRAP;
    }


    public BootstrapEndpointResponse bootstrapEndpoint(AgentDeploymentRequest managedEndpoints) {
        PrintUtils.printToActor("Received Agent Management Request");

        UUID requestID = getUUIDGen().generateNewUUID();

        // Get BootstrapActor reference and submit the request. The request is in turn gets routed to appropriate
        // actor routees.
        this.getActor().tell(new BootstrapActor.BootstrapEndpoint(managedEndpoints, requestID),
                ActorRef.noSender());

        return new BootstrapEndpointResponse(requestID.toString());
    }

}
