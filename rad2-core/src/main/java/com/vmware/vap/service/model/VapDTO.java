package com.vmware.vap.service.model;

/**
 * Representation of a VAP resource Instance
 */
public class VapDTO {
    private String vapName;
    private String cloudProxyId;
    private String agentId;

    public VapDTO() {
    }

    public VapDTO(String vapName, String leMansAgentId, String cloudProxyId) {
        this.vapName = vapName;
        this.cloudProxyId = cloudProxyId;
        this.agentId = leMansAgentId;
    }

    public String getVapName() {
        return vapName;
    }

    public String getCloudProxyId() {
        return cloudProxyId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }
}
