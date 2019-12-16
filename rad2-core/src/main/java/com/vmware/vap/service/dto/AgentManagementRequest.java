package com.vmware.vap.service.dto;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.vmware.vap.service.VapServiceUtils;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


//TODO - modify the request and response object with a generic framework
public class AgentManagementRequest {

    private AgentManagementDTO agentManagement;
    private String lemansAgentId;
    private String cloudProxyId;
    @ApiModelProperty(name="jobData", hidden = true)
    private String jobData;

    public AgentManagementDTO getAgentManagement() {
        return agentManagement;
    }

    public String getLemansAgentId() {
        return lemansAgentId;
    }

    public String getCloudProxyId() {
        return cloudProxyId;
    }

    public String getJobData(){
        Map<String, Object> jobData = new HashMap<>();
        jobData.put(AgentManagementDTO.class.getName(), agentManagement);
        return VapServiceUtils.convertToJson(jobData, new TypeToken<Map<String, Object>>() {
        }.getType());

    }

    public static class AgentManagementDTO {
        private List<EndpointSearchItemDTO> endpoints;
        private AgentManagementResponse.AgentAction operation;

        public List<EndpointSearchItemDTO> getEndpoints() {
            return endpoints;
        }

        public AgentManagementResponse.AgentAction getOperation() {
            return operation;
        }
        public void setOperation(AgentManagementResponse.AgentAction operation) {
            this.operation = operation;
        }

        public void setEndpoints(List<EndpointSearchItemDTO> endpoints) {
            this.endpoints = endpoints;
        }

        public enum Operation {
            @SerializedName("start")
            START("start"),

            @SerializedName("stop")
            STOP("stop"),

            @SerializedName("restart")
            RESTART("restart"),

            @SerializedName("content_upgrade")
            CONTENT_UPGRADE("content_upgrade");

            private String value;

            Operation(String value) {
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
    }

    @ApiModelProperty(name="agentManagementStateDTO", hidden = true)
    public AgentManagementResponse getAgentManagementStateDTO(){
        AgentManagementResponse agentManagementResponse = new AgentManagementResponse();
        agentManagementResponse.setAction(this.agentManagement.operation);
        agentManagementResponse.setAgentName("telegraf");
        List<String> vmList = new ArrayList<>();
        for(final EndpointSearchItemDTO endpointSearchItemDTO : this.agentManagement.endpoints ){
            for(final String vmMor : endpointSearchItemDTO.getVmMors()){
                vmList.add(VapServiceUtils.contructEndpointID(endpointSearchItemDTO.getVcUUID()
                            , vmMor));
            }
        }
        agentManagementResponse.setVmID(vmList.toArray(new String[vmList.size()]));
        return agentManagementResponse;
    }

    public void setAgentManagement(AgentManagementDTO agentManagement) {
        this.agentManagement = agentManagement;
    }

}
