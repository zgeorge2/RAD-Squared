package com.vmware.vap.service.dto;

/**
 * BootstrapEndpointResponse
 */
public class BootstrapEndpointResponse {
    String requestId;

    public BootstrapEndpointResponse(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
