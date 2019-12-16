package com.vmware.vap.service.control;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vmware.common.constants.CspAuthTokenResponse;
import com.vmware.common.constants.ServiceConfig;
import com.vmware.common.constants.VapServiceConstants;
import com.vmware.ignite.common.RegistryManager;
import com.vmware.ignite.common.SystemConfigRegistry;
import com.vmware.ignite.common.UsesRegistryManager;
import com.vmware.ignite.util.JobType;
import com.vmware.lemans.common.model.Command;
import com.vmware.lemans.common.model.CommandExecution;
import com.vmware.lemans.common.model.CommandRequest;
import com.vmware.vap.service.dto.*;
import com.vmware.vap.service.exception.LemansServiceException;
import com.vmware.xenon.common.Service;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static com.vmware.common.constants.VapServiceConstants.*;

/**
 * Lemans delegator. This delegator acts as a bridge between SAAS and on-prem. Any request to SAAS will be
 * forwarded to Lemans-Command channel through the delegator. Job status of the method is periodically
 * checked, until the job completes.
 */
public class LemansOnPremVapDelegate implements OnPremVapDelegate, UsesRegistryManager {
    //constants that hold lemans related properties
    private static final String LEMANS_PROP = "lemans";
    public static final String LEMANS_URI = LEMANS_PROP + "." + "lemans_uri";
    public static final String LEMANS_TOKEN = LEMANS_PROP + "." + "lemans_auth_token";
    private static final String SYNCH_EXECUTOR = "/le-mans/synchronous-command-executor";
    private static final String PROXY_SYNCH_EXECUTOR = "/vap/proxy-synchronous-command-executor";
    private static final String LEMANS_COMMAND_API_PATH = "/le-mans/v1/resources/commands";
    private static Logger logger = LoggerFactory.getLogger(LemansOnPremVapDelegate.class);
    //constants that hold endpoint urls
    private RegistryManager rm;

    public LemansOnPremVapDelegate(RegistryManager rm) {
        this.rm = rm;
    }

    @Override
    public void bootstrapEndpoint(UUID requestID, AgentDeploymentRequest agentDetails) throws LemansServiceException {

        Command command = createCommand(agentDetails.getCloudProxyId(), requestID, true, Service.Action.POST);
        agentDetails.setDocumentSelfLink(requestID.toString());

        Gson gsonBuilder = new GsonBuilder().create();
        command.request.body = gsonBuilder.toJson(
            new OnPremProxyDTO(this.getSysProp(VapServiceConstants.BOOTSTRAP_API_PATH), Service.Action.POST,
                gsonBuilder.toJson(agentDetails)));
        String lemansUri = getLemansUri();
        if (StringUtils.isEmpty(lemansUri)) {
            logger.error("unable to fetch lemans uri for bootstrap request "
                + "with id {}", requestID);
            throw new LemansServiceException(
                "unable to fetch " + "lemans " + "uri for bootstrap request "
                    + "with id " + requestID);
        }

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.exchange(lemansUri, HttpMethod.POST, getHttpEntity(command),
            AgentDeploymentResponse.class).getBody();
    }

    @Override
    public <T> T checkJobStatus(UUID requestID, String agentId, JobType<T> jobType) throws LemansServiceException {

        Command command = createCommand(agentId, requestID, true, Service.Action.POST);

        OnPremProxyDTO proxyDTO =
            new OnPremProxyDTO(this.getSysProp(jobType.getUri()) + "/" + requestID.toString(),
            Service.Action.GET, null);
        Gson gsonBuilder = new GsonBuilder().create();
        command.request.body = gsonBuilder.toJson(proxyDTO);
        RestTemplate restTemplate = new RestTemplate();
        T t = restTemplate.exchange(getLemansUri(), HttpMethod.POST,
            getHttpEntity(command), jobType.getTClass()).getBody();
        return t;
    }

    private String getLemansUri() {
        ServiceConfig lemansService = VapServiceConstants.getService(VapServiceConstants.LEMANS_SERVICE_KEY);
        final String lemansUri = lemansService.getUrl();
        String uri = lemansUri + LEMANS_COMMAND_API_PATH;
        return uri;
    }

    @Override
    public void managePlugins(UUID requestID, PluginsDTO pluginsDTO) throws LemansServiceException {

        Command command = createCommand(pluginsDTO.getCloudProxyId(), requestID, true, Service.Action.POST);
        Gson gsonBuilder = new GsonBuilder().create();
        pluginsDTO.setDocumentSelfLink(requestID.toString());
        command.request.body = gsonBuilder.toJson(
            new OnPremProxyDTO(this.getSysProp(VapServiceConstants.PLUGINS_API_PATH), Service.Action.POST,
                gsonBuilder.toJson(pluginsDTO)));
        RestTemplate restTemplate = new RestTemplate();
        String lemansUri = getLemansUri();
        if (StringUtils.isEmpty(lemansUri)) {
            throw new LemansServiceException(
                "unable to fetch " + "lemans " + "uri for bootstrap request "
                    + "with id " + requestID);
        }
        restTemplate.exchange(lemansUri, HttpMethod.POST, getHttpEntity(command),
            AgentDeploymentResponse.class).getBody();
    }

    @Override
    public void manageAgent(UUID requestID, AgentManagementRequest agentManagementRequestDTO)
        throws LemansServiceException {
        Command command = createCommand(agentManagementRequestDTO.getCloudProxyId(),
            requestID, true, Service.Action.POST);
        AgentManagementResponse agentManagementResponse =
            agentManagementRequestDTO.getAgentManagementStateDTO();
        agentManagementResponse.setDocumentSelfLink(requestID.toString());
        Gson gsonBuilder = new GsonBuilder().create();
        command.request.body = gsonBuilder.toJson(
            new OnPremProxyDTO(this.getSysProp(VapServiceConstants.AGENT_MGMT_API_PATH), Service.Action.POST,
                gsonBuilder.toJson(agentManagementResponse)));
        RestTemplate restTemplate = new RestTemplate();
        String lemansUri = getLemansUri();
        if (StringUtils.isEmpty(lemansUri)) {
            throw new LemansServiceException(
                "unable to fetch " + "lemans " + "uri for bootstrap request "
                    + "with id " + requestID);
        }
        restTemplate.exchange(lemansUri, HttpMethod.POST, getHttpEntity(command),
            AgentManagementResponse.class);
    }

    @Override
    public ValidateCloudAccountResponse validateCloudAccount(UUID requestId,
                                                             ValidateCloudAccountRequest validateCloudAccountRequest) throws
        LemansServiceException {

        Command command = createCommand(validateCloudAccountRequest.getLemansAgentId(), requestId, true,
            Service.Action.POST);
        Gson gsonBuilder = new GsonBuilder().create();
        command.request.body = gsonBuilder.toJson(
            new OnPremProxyDTO(this.getSysProp(VapServiceConstants.CLOUD_ACCOUNT_API_PATH),
                Service.Action.POST,
                gsonBuilder.toJson(new ValidateCloudAccountRequest.OnPremRequest(requestId,
                    validateCloudAccountRequest))));
        RestTemplate restTemplate = new RestTemplate();
        String lemansUri = getLemansUri();
        ValidateCloudAccountResponse.OnPremResponse onPremResponse = restTemplate.exchange(lemansUri,
            HttpMethod
            .POST, getHttpEntity(command), ValidateCloudAccountResponse.OnPremResponse.class).getBody();
        return onPremResponse.getResponseDTO();
    }

    private HttpEntity<Command> getHttpEntity(Command command) {
        HttpHeaders headers = new HttpHeaders();
        ServiceConfig lemansService = VapServiceConstants.getService(VapServiceConstants.LEMANS_SERVICE_KEY);
        headers.set("x-xenon-auth-token", lemansService.getProps().get("lemans_auth_token"));
        headers.set("Content-Type", "application/json");
        HttpEntity<Command> httpEntity = new HttpEntity<>(command, headers);
        return httpEntity;
    }

    private Command createCommand(String cloudProxyId, UUID requestId, boolean isDirect,
                                  Service.Action action) throws
        LemansServiceException {
        Command command = new Command();
        String lemansAgentId = getLeMansAgentID(cloudProxyId);
        command.agentId = lemansAgentId;
        final String uuid = requestId.toString();
        command.id = uuid;
        command.executionState = new CommandExecution();
        command.executionState.executorLink = SYNCH_EXECUTOR;
        command.isDirect = isDirect;
        command.documentSelfLink = uuid;
        command.request = new CommandRequest();

        command.request.action = action;
        command.request.path = PROXY_SYNCH_EXECUTOR;

        return command;
    }

    private SystemConfigRegistry getSCReg() {
        return reg(SystemConfigRegistry.class);
    }

    private String getSysProp(String path) {
        return this.getSCReg().getSysProp(path);
    }

    @Override
    public RegistryManager getRM() {
        return this.rm;
    }

    public String getLeMansAgentID(String cloudProxyId) throws LemansServiceException {

        ServiceConfig lemansService = VapServiceConstants.getService(VapServiceConstants.LEMANS_SERVICE_KEY);
        String lemans_url = lemansService.getUrl() + LEMANS_AGENT_RESOURCES;
        String token;
        LemansAgentResponse agentResponse = new LemansAgentResponse();
        try {
            if (StringUtils.isNotBlank(lemansService.getProps().get("lemans_auth_token"))) {
                token = lemansService.getProps().get("lemans_auth_token");
            } else {
                CspAuthTokenResponse tokenResponse =
                    getCspClientCredentials(lemansService.getAuth().get(CSP_AUTH_ID_KEY),
                    lemansService.getAuth().get(CSP_AUTH_SECRET_KEY));
                if (tokenResponse == null || StringUtils.isBlank(tokenResponse.getAccessToken())) {
                    throw new LemansServiceException("could not retrieve auth token to reach lemans " +
                        "resources api");
                }
                token = tokenResponse.getAccessToken();
            }

            RestTemplate restTemplate = new RestTemplate();
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(lemans_url)
                .queryParam("$filter", "type eq vap-agent");
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            //headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("x-xenon-auth-token", token);
            HttpEntity entity = new HttpEntity("", headers);
            ResponseEntity<String> lemansAgents = restTemplate
                .exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);
            if (lemansAgents.getStatusCode() == HttpStatus.OK && StringUtils.isNotBlank(lemansAgents.getBody())) {
                JsonObject lemansAgtJson = new Gson().fromJson(lemansAgents.getBody(), JsonObject.class);
                if (lemansAgtJson.has("documents")) {
                    JsonObject documents = lemansAgtJson.getAsJsonObject("documents");
                    for (Map.Entry<String, JsonElement> entry : documents.entrySet()) {
                        JsonObject document = entry.getValue().getAsJsonObject();
                        if (document.getAsJsonObject("customProperties").has("proxyId")) {
                            if (cloudProxyId
                                .equalsIgnoreCase(document.getAsJsonObject("customProperties").get("proxyId"
                                ).getAsString())) {
                                agentResponse.id = document.get("id").getAsString();
                                agentResponse.tenantId = document.get("tenantId").getAsString();
                                agentResponse.type = document.get("type").getAsString();
                                agentResponse.rdcId = cloudProxyId;
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw new LemansServiceException(ex.getMessage(), ex);
        }
        if (StringUtils.isNotBlank(agentResponse.id)) {
            return agentResponse.id;
        } else {
            throw new LemansServiceException("Unable to fetch agent id for the given cloud proxy id " + cloudProxyId);
        }
    }

    public CspAuthTokenResponse getCspClientCredentials(String cspServiceAuthID, String cspAuthSecret) {

        RestTemplate restTemplate = new RestTemplate();
        String cspUri = VapServiceConstants.CSP_URI;
        //        String clientId = this.cspServiceAuthID;
        //        String clientSecret = this.cspAuthSecret;
        if (cspServiceAuthID != null && cspAuthSecret != null && cspUri != null) {
            HttpHeaders headers = new HttpHeaders();
            String encoded = Base64.getEncoder().encodeToString(String.format("%s:%s", cspServiceAuthID,
                cspAuthSecret).getBytes(StandardCharsets.UTF_8));
            headers.add("Authorization", String.format("Basic %s", encoded));
            headers.add("Content-Type", "application/x-www-form-urlencoded");
            HttpEntity<String> entity = new HttpEntity("grant_type=client_credentials", headers);
            String url = String.format("%s/csp/gateway/am/api/login/oauth", cspUri);
            return restTemplate.postForObject(url, entity, CspAuthTokenResponse.class, new Object[0]);
        } else {
            logger.error((String.format("clientId %s, clientSecret %s and cspUri %s cannot be null",
                cspServiceAuthID,
                cspAuthSecret, cspUri)));
            return null;
        }
    }

    private class LemansAgentResponse {
        /***
         * "id": "77658b57-9c95-455a-ac38-66636639f700",
         *             "tenantId": "30a8b76062f72ea7",
         *             "type": "vap-agent",
         *             "version": 1,
         *             "certEnc":
         *             "MIICrDCCAZSgAwIBAgIEZBlA8TANBgkqhkiG9w0BAQ0FADAYMRYwFAYDVQQDEw1MRU1BTlMtQ0xJRU5UMB4XDTE4MTEyMzEzMTAxNloXDTE5MTEyMzEzMTAxNlowGDEWMBQGA1UEAxMNTEVNQU5TLUNMSUVOVDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAIVCuylamlJHhIYUBh5xtS7tI6sny/RTnLiXBZc4Sk1ZUht7fM6l1BipBJ/ZzJfLhmA/6jKnZl3ed6U5N6ja/zHw9fIB3tqQvt6NcOSYI3z8XXg9a9c//W2qV0DqIvkNM4+zte3AGoY+Tc9otqbww0v2NGABVuzRrx0GGd/mq+0pLgiLBgeN/PCeSdbsqSL+liDU2IHaGyfR+rWepYekSTaxSpaksJzKgmTuUW98H48VtLWzbYMVIF7YLTO4UdHovjc4UwOA035wy1shfVutP43RnNTsb+tf1xj3kBZH7GcYVMYmFavjCAf7ELVn3LXJ/oRdOX1meK61TM/jPQN860sCAwEAATANBgkqhkiG9w0BAQ0FAAOCAQEAWYjWwSaYVvC3njSDwYOZcoYTwtZKAWp2bgREedp7QoyiQY7Imnk5C2Mk2SlAHuWJqrqq8UOlV5v/UdzeBnVc67LbqMR/6Hx9Kwnv8M7M6Kl0NpOAiGZdYw1GF0U574GizC+6P6hcpn8CDQv6M+bjBp35Xq2kpgzZOh2Ln9SOof8tYQIdP9enEAQnNsVPacDGVYg0IU+rR1tnTlukF5q+qtHRzLPH9BIKPKGfSi5YaOlNGorWprazWxDJS0a9B6PkDR8Nhw7J/I+2e+tc5r6YzhydbxZY8di3n9sc0zmCv7qaoPrOEFwya9MfdP955SVSktUt+sano9T0wkmILrqTig==",
         *             "customProperties": {
         *                 "proxyId": "rahav-vap"
         *             }
         */
        private String id;
        private String tenantId;
        private String type;
        private String rdcId;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTenantId() {
            return tenantId;
        }

        public void setTenantId(String tenantId) {
            this.tenantId = tenantId;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getRdcId() {
            return rdcId;
        }

        public void setRdcId(String rdcId) {
            this.rdcId = rdcId;
        }
    }
}
