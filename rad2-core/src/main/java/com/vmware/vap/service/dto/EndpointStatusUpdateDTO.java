package com.vmware.vap.service.dto;

/**
 * The DTO representing a status update from endpoint
 *
 */
public class EndpointStatusUpdateDTO {

    private String endpointID;

    private String lastOperationStatus;

    private String endpointState;

    public EndpointStatusUpdateDTO(String endpointID, String endpointStatus, String endpointState) {
        super();
        this.endpointID = endpointID;
        this.lastOperationStatus = endpointStatus;
        this.endpointState = endpointState;
    }

    public String getEndpointID() {
        return endpointID;
    }

    public String getLastOperationStatus() {
        return lastOperationStatus;
    }

    public String getEndpointState() {
        return endpointState;
    }

    public void setLastOperationStatus(String lastOperationStatus) {
        this.lastOperationStatus = lastOperationStatus;
    }

    public void setEndpointState(String endpointState) {
        this.endpointState = endpointState;
    }

}
