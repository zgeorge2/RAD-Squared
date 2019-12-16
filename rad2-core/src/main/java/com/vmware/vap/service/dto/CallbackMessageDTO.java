package com.vmware.vap.service.dto;

import com.vmware.vap.bridge.common.MessageType;

/**
 * The DTO for representing a Callback payload from onPrem via Lemans
 */
public class CallbackMessageDTO {

    MessageType messageType;
    String payload;
    CallbackMessageHeader messageHeader;

    public CallbackMessageDTO() {
    }

    public CallbackMessageDTO(
                MessageType messageType,
                String payload,
                CallbackMessageHeader messageHeader) {
        this.messageType = messageType;
        this.payload = payload;
        this.messageHeader = messageHeader;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public String getPayload() {
        return payload;
    }

    public CallbackMessageHeader getMessageHeader() {
        return messageHeader;
    }

    public String getTenantId() {
        return messageHeader.getTenantId();
    }

    public String getRdcId() {
        return messageHeader.getRdcId();
    }

    public String getAgentId() {
        return messageHeader.getAgentId();
    }

    // TODO: remove this method once id is incorporated into EPRegistry
    public String getVapName() {
        return messageHeader.getAgentId();
    }

    /**
     * MessageHeader
     */
    public static class CallbackMessageHeader {
        String tenantId;
        String rdcId;
        String agentId;

        public CallbackMessageHeader(
          String tenantId, String rdcId, String agentId) {
            this.tenantId = tenantId;
            this.rdcId = rdcId;
            this.agentId = agentId;
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


        @Override
        public String toString() {
            return String.format("%s, %s, %s", tenantId, rdcId,
                        agentId);
        }
    }
}