package com.vmware.vap.service.dto;

import com.vmware.xenon.common.Service;

public class OnPremProxyDTO {

    public String uri;
    public Service.Action action;
    public String body;

    public OnPremProxyDTO(String uri, Service.Action action, String body){
        this.uri = uri;
        this.action = action;
        this.body = body;
    }
}
