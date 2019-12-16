package com.vmware.vap.service.dto;

import com.vmware.xenon.services.common.TaskService;
import state.ManageEndpointServiceState;
import state.ServiceErrorResponse;

import java.util.Map;

/**
 * DTO for handling agent management Response.
 */
public class AgentDeploymentResponse extends TaskService.TaskServiceState {
    private ManageEndpointServiceState[] manageEndpointServiceStates;
    private String statusMsg;
    /**
     * map having vcid_vmid as key and bootstrap status as value. - (SUCCESS, FAILED, IN_PROGRESS)
     */
    private Map<String, String> vmStatus;
    /**
     * map having vcid_vmid as key and step and by step progress during bootstrap.
     */
    private Map<String, String> vmProgressStatus;
    private String docLink;
    private JOB job;
    private ServiceErrorResponse serviceErrorResponse;
    private String message;
    private int statusCode;
    private int errorCode;

    /**
     * The types of Jobs
     *
     */
    public enum JOB {
        install,
        uninstall,
        remap
    }

    public ManageEndpointServiceState[] getManageEndpointServiceStates() {
        return manageEndpointServiceStates;
    }

    public void setManageEndpointServiceStates(ManageEndpointServiceState[] manageEndpointServiceStates) {
        this.manageEndpointServiceStates = manageEndpointServiceStates;
    }

    public String getStatusMsg() {
        return statusMsg;
    }

    public Map<String, String> getVmStatus() {
        return vmStatus;
    }

    public Map<String, String> getVmProgressStatus() {
        return vmProgressStatus;
    }

    public String getDocLink() {
        return docLink;
    }

    public JOB getJob() {
        return job;
    }

    public ServiceErrorResponse getServiceErrorResponse() {
        return serviceErrorResponse;
    }

    public void setServiceErrorResponse(ServiceErrorResponse serviceErrorResponse) {
        this.serviceErrorResponse = serviceErrorResponse;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}
