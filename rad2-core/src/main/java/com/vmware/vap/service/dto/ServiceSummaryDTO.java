package com.vmware.vap.service.dto;

public class ServiceSummaryDTO {

    private String serviceName;
    private ServiceState serviceState;
    private long lastCollectedTime;

    public enum ServiceState {
        DISCOVERED, ACTIVATING, DE_ACTIVATING, COLLECTING, ACTIVATION_SUCCEEDED, ACTIVATION_FAILED, COLLECTION_FAILED,
        COLLECTION_SUCCEEDED
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public ServiceState getServiceState() {
        return serviceState;
    }

    public void setServiceState(ServiceState serviceState) {
        this.serviceState = serviceState;
    }

    public long getLastCollectedTime() {
        return lastCollectedTime;
    }

    public void setLastCollectedTime(long lastCollectedTime) {
        this.lastCollectedTime = lastCollectedTime;
    }
}
