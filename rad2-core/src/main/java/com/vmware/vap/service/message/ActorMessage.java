package com.vmware.vap.service.message;

import com.vmware.vap.bridge.common.MessageType;
import com.vmware.vap.service.dto.CallbackMessageDTO;

/**
 * Actor Message - A base class to all types of call back messages passed to VAP Actors
 */
public class ActorMessage {
    MessageType messageType;
    String tenantId;
    String rdcId;
    String agentId;
    String payload;

    public ActorMessage(CallbackMessageDTO messageDTO) {
        this.messageType = messageDTO.getMessageType();

        this.tenantId = messageDTO.getTenantId();
        this.rdcId = messageDTO.getRdcId();
        this.agentId = messageDTO.getAgentId();

        this.payload = messageDTO.getPayload();
    }

    // TODO: remove this method once id is incorporated into EPRegistry
    public String getVapName() {
        return agentId;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getRdcId() {
        return rdcId;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getPayload() {
        return payload;
    }
}
