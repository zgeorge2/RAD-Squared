package com.vmware.vap.service.dto;

import java.util.List;

public class EndpointSearchItemDTO {

    private String vcUUID;
    private List<String> vmMors;

    public String getVcUUID() {
        return vcUUID;
    }

    public void setVcUUID(String vcUUID) {
        this.vcUUID = vcUUID;
    }

    public List<String> getVmMors() {
        return vmMors;
    }

    public void setVmMors(List<String> vmMors) {
        this.vmMors = vmMors;
    }
}
