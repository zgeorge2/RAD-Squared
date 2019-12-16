package com.vmware.vap.service.dto;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.vmware.vap.service.VapServiceUtils;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The DTO for handling Agent Management Requests
 */
public class AgentDeploymentRequest {
    private String lemansAgentId;
    private String cloudProxyId;
    private List<ManageEndpointsDTO> manageEndpointServiceStates;
    private OperationType job;
    @ApiModelProperty(hidden = true)
    private String documentSelfLink;

    public AgentDeploymentRequest() {
        manageEndpointServiceStates = new ArrayList<>();
    }

    public List<ManageEndpointsDTO> getManageEndpointServiceStates() {
        return manageEndpointServiceStates;
    }

    public String getDocumentSelfLink() {
        return documentSelfLink;
    }

    public void setDocumentSelfLink(String documentSelfLink) {
        this.documentSelfLink = documentSelfLink;
    }


    public String getLemansAgentId() {
        return lemansAgentId;
    }

    public OperationType getJob() {
        return job;
    }

    public String getCloudProxyId() {
        return cloudProxyId;
    }

    @ApiModelProperty(name="jobData", hidden = true)
    public String getJobData(){
        final List<ManageEndpointsDTO> manageEndpointsDTOS = new ArrayList<>(manageEndpointServiceStates);
        for(final ManageEndpointsDTO manageEndpointsDTO : manageEndpointsDTOS){
            for(final EndPointDTO endpointDTO : manageEndpointsDTO.endpoints){
                endpointDTO.setPassword("");
            }
        }
        Map<String, Object> jobData = new HashMap<>();
        jobData.put(ManageEndpointsDTO.class.getCanonicalName(), manageEndpointsDTOS);
        jobData.put(OperationType.class.getName(), job);
        return VapServiceUtils.convertToJson(jobData, new TypeToken<Map<String, Object>>() {
        }.getType());
    }

    public enum OperationType {
        @SerializedName("install")
        install("install"),
        @SerializedName("uninstall")
        uninstall("uninstall"),
        @SerializedName("remap")
        remap("remap");
        private String value;

        OperationType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    public static class ManageEndpointsDTO {
        private String vc_ip;
        private String vc_user;
        private String vc_password;
        private List<EndPointDTO> endpoints;

        public String getVc_ip() {
            return vc_ip;
        }

        public String getVc_user() {
            return vc_user;
        }

        public String getVc_password() {
            return vc_password;
        }

        public List<EndPointDTO> getEndpoints() {
            return endpoints;
        }
    }

    public static class JobStatusDTO {
        private String jobType;
        private String jobStatus;

        public String getJobType() {
            return this.jobType;
        }

        public String getJobStatus() {
            return this.jobStatus;
        }
    }

    public static class EndPointDTO {
        private String vc_id;
        private String vm_mor;
        private String user;
        private String password;
        private String jobId;

        public String getVc_id() {
            return this.vc_id;
        }

        public String getVm_mor() {
            return this.vm_mor;
        }

        public String getUser() {
            return this.user;
        }

        public String getPassword() {
            return this.password;
        }


        public String getJobId() {
            return this.jobId;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
