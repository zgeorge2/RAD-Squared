package com.vmware.sb.apps.vap;


import com.vmware.common.constants.ServiceConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "integration")

public class IntegrationConfig {

    private List<ServiceConfig> services = new ArrayList<>();

    public List<ServiceConfig> getServices() {
        return services;
    }

    public void setServices(List<ServiceConfig> services) {
        this.services = services;
    }

}

