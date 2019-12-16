package com.vmware.ignite.util;

import com.vmware.common.constants.VapServiceConstants;
import com.vmware.vap.service.dto.AgentDeploymentResponse;
import com.vmware.vap.service.dto.AgentManagementResponse;
import com.vmware.vap.service.dto.PluginManagementResponse;
import com.vmware.vap.service.dto.ValidateCloudAccountResponse;

public final class JobType<T> {

    private String name;
    private String uri;
    private Class<T> tClass;

    public static final JobType<AgentDeploymentResponse> AGENT_DEPLOYMENT =
                new JobType<>(VapServiceConstants.AGENT_DEPLOYMENT,
                VapServiceConstants.BOOTSTRAP_API_PATH, AgentDeploymentResponse.class);

    public static final JobType<AgentManagementResponse> AGENT_MANAGEMENT =
                new JobType<>(VapServiceConstants.AGENT_MANAGEMENT,
                VapServiceConstants.AGENT_MGMT_API_PATH, AgentManagementResponse.class);

    public static final JobType<PluginManagementResponse> PLUGIN_MANAGEMENT =
                new JobType<>(VapServiceConstants.PLUGIN_MANAGEMENT,
                VapServiceConstants.PLUGINS_API_PATH, PluginManagementResponse.class);

    public static final JobType<ValidateCloudAccountResponse> CLOUD_ACCOUNT_VALIDATION =
            new JobType<>(VapServiceConstants.CLOUD_ACCOUNT_VALIDATION,
                    VapServiceConstants.CLOUD_ACCOUNT_API_PATH, ValidateCloudAccountResponse.class);

    private JobType(String name, String uri, Class<T> tClass) {
        this.name = name;
        this.uri = uri;
        this.tClass = tClass;
    }

    public String getName(){
        return name;
    }

    public String getUri() {
        return uri;
    }

    public Class<T> getTClass() {
        return tClass;
    }
}
