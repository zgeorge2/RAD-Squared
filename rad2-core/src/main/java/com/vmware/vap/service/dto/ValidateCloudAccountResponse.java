package com.vmware.vap.service.dto;


import com.vmware.xenon.services.common.TaskService;

import javax.validation.ValidationProviderResolver;
import java.util.UUID;


public class ValidateCloudAccountResponse {
    CloudAccountType type;
    String ipOrFqdn;
    String username;
    String status;
    String statusMesg;

    public ValidateCloudAccountResponse(CloudAccountType type, String ipOrFqdn, String username, String status,
                                        String statusMesg) {
        this.type = type;
        this.ipOrFqdn = ipOrFqdn;
        this.username = username;
        this.status = status;
        this.statusMesg = statusMesg;
    }

    public ValidateCloudAccountResponse(ValidateCloudAccountRequest request, String status, String statusMesg) {
        this.type = request.getType();
        this.ipOrFqdn = request.getIpOrFqdn();
        this.status = status;
        this.statusMesg = statusMesg;
    }

    public CloudAccountType getType() {
        return type;
    }

    public String getIpOrFqdn() {
        return ipOrFqdn;
    }

    public String getUsername() {
        return username;
    }

    public String getStatus() {
        return status;
    }

    public String getStatusMesg() {
        return statusMesg;
    }

    public static class OnPremResponse extends TaskService.TaskServiceState {
        public CloudAccountType type;
        public String ipOrFqdn;
        public String username;
        public boolean guestOpsAllowed;
        public String statusMesg;
        public String status;
        public String docLink;

        public OnPremResponse() {

        }

        public ValidateCloudAccountResponse getResponseDTO() {
            return new ValidateCloudAccountResponse(type, ipOrFqdn, username, status, statusMesg);
        }
    }
}
