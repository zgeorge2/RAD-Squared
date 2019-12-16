package com.vmware.vap.service.dto;


import com.vmware.xenon.services.common.TaskService;

import java.util.UUID;

public class ValidateCloudAccountRequest {
    CloudAccountType type;
    String ipOrFqdn;
    String username;
    String password;
    String cloudProxyId;
    String lemansAgentId;

    public CloudAccountType getType() {
        return type;
    }

    public String getIpOrFqdn() {
        return ipOrFqdn;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getCloudProxyId() {
        return cloudProxyId;
    }

    public String getLemansAgentId() {
        return lemansAgentId;
    }


    public static class OnPremRequest extends TaskService.TaskServiceState {
        ValidateCloudAccountRequest validateCloudAccountRequest;

        public OnPremRequest(UUID requestId, ValidateCloudAccountRequest validateCloudAccountRequest) {
            this.documentSelfLink = requestId.toString();
            this.validateCloudAccountRequest = validateCloudAccountRequest;
        }
    }
}
