package com.vmware.vap.service.dto;

import com.vmware.apps.vap.ignite.ServiceRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EndpointDTO {
    private String vcUUID;
    private String vmMor;
    private AgentState agentState;
    private LastOperationStatus lastOperationStatus;
    private long lastCollectedTimestamp;
    private List<ServiceRegistry.DService> serviceDtos;

    public String getVcUUID() {
        return vcUUID;
    }

    public void setVcUUID(String vcUUID) {
        this.vcUUID = vcUUID;
    }

    public String getVmMor() {
        return vmMor;
    }

    public void setVmMor(String vmMor) {
        this.vmMor = vmMor;
    }

    public AgentState getAgentState() {
        return agentState;
    }

    public void setAgentState(AgentState agentState) {
        this.agentState = agentState;
    }

    public LastOperationStatus getLastOperationStatus() {
        return lastOperationStatus;
    }

    public void setLastOperationStatus(LastOperationStatus lastOperationStatus) {
        this.lastOperationStatus = lastOperationStatus;
    }

    public long getLastCollectedTimestamp() {
        return lastCollectedTimestamp;
    }

    public void setLastCollectedTimestamp(long lastCollectedTimestamp) {
        this.lastCollectedTimestamp = lastCollectedTimestamp;
    }

    public List<ServiceRegistry.DService> getServiceDtos() {
        return serviceDtos;
    }

    public void setServiceDtos(List<ServiceRegistry.DService> serviceDtos) {
        this.serviceDtos = serviceDtos;
    }

    public enum AgentState {
        RUNNING, STOPPED, NOT_INSTALLED;


    }

    public enum LastOperationStatus {
        INSTALL_SUCCESS, INSTALL_FAILED, INSTALL_IN_PROGRESS,
        UNINSTALL, UNINSTALL_FAILED, UNINSTALL_IN_PROGRESS,
        START, START_FAILED, START_IN_PORGRESS,
        STOP, STOP_FAILED, STOP_IN_PROGRESS,
        CONTENT_UPGRADE, CONTENT_UPGRADE_FAILED, CONTENT_UPGRADE_IN_PROGRESS;

        private static Map<String, LastOperationStatus> statusMap = new HashMap<>();

        static {
            for (LastOperationStatus lastOperationStatus : LastOperationStatus.values()) {
                statusMap.put(lastOperationStatus.toString().toLowerCase(), lastOperationStatus);
            }
        }

        public static LastOperationStatus getAgentStatus(String status){
            return statusMap.get(status.toLowerCase());
        }
    }

}

