package com.vmware.common.constants;

import java.util.ArrayList;
import java.util.List;

/**
 * ServiceConfig Constants
 */
public class VapServiceConstants {
    public static final int LEMANS_SYNC_CMD_TIMEOUT_SECS = 50;
    public static final String VC_UUID = "vcUUID";
    public static final String VM_MOR = "vmMOR";
    public static final int GET_VM_API_TIMEOUT_IN_SEC = 10;
    public static final String VCID = "vc_uuid";
    public static final String VM_ID = "vm_mor";
    public static final String INSTALL = "install";
    public static final String UNINSTALL = "uninstall";
    public static final String SERVICENAME = "serviceName";
    public static final String PLUGINNAME = "pluginName";
    public static final String STATE = "state";
    public static final String STATUS = "status";
    public static final String AGENT_NAME = "agentName";
    public static final String AGENT_ACTION = "action";
    public static final String BOOTSTRAP_STATUS = "bootstrap_status";
    public static final String ATTR_VC_ID_KEY = "VC_ID_KEY";
    public static final String ATTR_VM_MOR_KEY = "VM_MOR_KEY";
    public static final String ATTR_ENDPOINT_STATE_KEY = "ENDPOINT_STATE_KEY";
    public static final String IN_PROGRESS = "IN_PROGRESS";
    public static final String SUCCESS = "SUCCESS";
    public static final String FAILED = "FAILED";
    public static final String ATTR_JOB_TYPE_KEY = "JOB_TYPE_KEY";
    public static final String ATTR_JOB_STATUS_KEY = "JOB_STATUS_KEY";
    public static final String ATTR_JOB_STARTED_KEY = "JOB_STARTED_KEY";
    public static final String ATTR_JOB_ID_KEY = "JOB_ID_KEY";
    public static final String ATTR_AGENT_ID_KEY = "AGENT_ID_KEY";
    public static final String ATTR_JOB_REQUEST_KEY = "JOB_REQ_KEY";
    public static final String ATTR_JOB_ENDPOINT_STATUS = "JOB_ENDPOINT_STATUS";
    public static final String SERVICE = "service";
    public static final String CONTENT_VERSION = "sd_content_version";
    public static final String ENDPOINT_DELIMITER = "_";
    public static final String LEMANS_SERVICE_KEY = "lemans";
    public static final String LEMANS_AGENT_RESOURCES = "/le-mans/v1/resources/agents?expand";
    public static final String REQUEST_ID = "requestId";
    //constants for CSP auth config for external services
    public static final String CSP_AUTH_SECRET_KEY = "cspAuthSecret";
    public static final String CSP_AUTH_ID_KEY = "cspAuthId";
    public static final String FINISHED = "FINISHED";
    //constants for CSP config related to VAP service
    public static String CSP_URI = "";
    public static String VAP_CSP_AUTH_ID = "";
    public static String VAP_CSP_AUTH_SECRET = "";
    //holds all the external configured services for VAP in application.
    public static List<ServiceConfig> services = new ArrayList<>();

    public static ServiceConfig getService(String serviceName) {
        for (ServiceConfig service : services) {
            if (serviceName.equalsIgnoreCase(service.getName())) {
                return service;
            }
        }
        return null;
    }

    public static final String VAPAPI_PROP = "vapapi";
    public static final String BOOTSTRAP_API_PATH = VAPAPI_PROP + "." + "bootstrap_url";
    public static final String PLUGINS_API_PATH = VAPAPI_PROP + "." + "plugins_url";
    public static final String AGENT_MGMT_API_PATH = VAPAPI_PROP + "." + "agentmgmt_url";
    public static final String CLOUD_ACCOUNT_API_PATH = VAPAPI_PROP + "." + "cloud_account_url";
    public static final String AGENT_DEPLOYMENT = "AGENT_DEPLOYMENT";
    public static final String AGENT_MANAGEMENT = "AGENT_MANAGEMENT";
    public static final String PLUGIN_MANAGEMENT = "PLUGIN_MANAGEMENT";
    public static final String CLOUD_ACCOUNT_VALIDATION = "CLOUD_ACCOUNT_VALIDATION";
    public static final String VAP_SAAS = "/vap-saas";
    public static final String VAP_API = VAP_SAAS + "/api";
    public static final String VAP_SAAS_JOBS = "/jobs";
    public static final String VAP_SAAS_JOB_API = VAP_API + VAP_SAAS_JOBS;
    public static final String AGENT_DEPLOYEMENT_URL = VAP_SAAS_JOB_API + "/agentdeployment";
    public static final String AGENT_MANAGEMENT_URL = VAP_SAAS_JOB_API + "/agentmanagement";
    public static final String PLUGIN_MANAGEMENT_URL = VAP_SAAS_JOB_API + "/pluginmanagement";
    public static final String ACTIVATE_PREFIX = "/activate";
    public static final String DEACTIVATE_PREFIX = "/deactivate";
    public static final String CLOUD_MANAGEMENT_URL = VAP_API + "/cloudaccount";
    public static final String VALIDATE_JOB_PREFIX = VAP_SAAS_JOBS + "/validate";
    public static final String INFRA_MANAGEMENT_URL = VAP_SAAS + "/infra";
    public static final String CALLBACK_URL = VAP_SAAS + "/callback";
    public static final String CONTROLPLANE_ACTION_PREFIX = "/controlplaneactions";
    public static final String ENDPOINTSTATE_PREFIX = "/endpointstate";
    public static final String VAPSTATE_PREFIX = "/vapstate";
    public static final String PROCESS_PREFIX = "/process";
    public static final String SEARCH_URL = VAP_API + "/search";
    public static final String MANAGE_ENDPOINTS_PREFIX = "/manageendpoints";
}
