package com.vmware.vap.service.dto;

import com.vmware.xenon.services.common.TaskService;

import java.util.Map;

public class AgentManagementResponse extends TaskService.TaskServiceState {

    private AgentAction action;
    private String agentName;
    private String[] vmID;
    private Map<String, String> vmStatus;
    private Map<String, String> failedVMs;
    private String requestId;

    public enum AgentAction {
        start, stop, restart, content_upgrade;
    }

    public AgentAction getAction() {
        return action;
    }

    public String getAgentName() {
        return agentName;
    }

    public String[] getVmID() {
        return vmID;
    }

    public Map<String, String> getVmStatus() {
        return vmStatus;
    }

    public Map<String, String> getFailedVMs() {
        return failedVMs;
    }

    public String getDocumentSelfLink() {
        return documentSelfLink;
    }

    public void setAction(AgentAction action) {
        this.action = action;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public void setVmID(String[] vmID) {
        this.vmID = vmID;
    }

    public void setVmStatus(Map<String, String> vmStatus) {
        this.vmStatus = vmStatus;
    }

    public void setFailedVMs(Map<String, String> failedVMs) {
        this.failedVMs = failedVMs;
    }

    public void setDocumentSelfLink(String documentSelfLink) {
        this.documentSelfLink = documentSelfLink;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
