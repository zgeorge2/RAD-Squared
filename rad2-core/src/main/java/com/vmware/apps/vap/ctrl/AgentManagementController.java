package com.vmware.apps.vap.ctrl;

import akka.actor.ActorRef;
import com.vmware.apps.vap.akka.AgentManagementActor;
import com.vmware.common.utils.PrintUtils;
import com.vmware.vap.service.control.VapActorType;
import com.vmware.vap.service.dto.AgentManagementRequest;
import com.vmware.vap.service.dto.AgentManagementResponse;

import java.util.UUID;

public class AgentManagementController extends BaseVAPController {

    public AgentManagementResponse manageAgent(AgentManagementRequest agentManagementDTO) {
        PrintUtils.printToActor("Received Agent Management Request");

        UUID requestID = getUUIDGen().generateNewUUID();

        this.getActor().tell(new AgentManagementActor.AgentManagement(requestID, agentManagementDTO),
                    ActorRef.noSender());
        AgentManagementResponse agentManagementResponse =
          new AgentManagementResponse();

        agentManagementResponse.setRequestId(requestID.toString());

        return agentManagementResponse;
    }

    @Override
    protected VapActorType getActorType() {
        return VapActorType.AGENT_MANAGEMENT;
    }
}
