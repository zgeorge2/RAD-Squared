package com.vmware.sb.apps.vap;

import com.vmware.common.constants.CspAuthTokenResponse;
import com.vmware.common.constants.ServiceConfig;
import com.vmware.common.constants.VapServiceConstants;
import com.vmware.apps.vap.ctrl.InfraHookController;
import com.vmware.sb.res.BaseResource;
import com.vmware.symphony.csp.auth.service.CspSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;


/**
 * The VAPResource provides REST resources for use by external entities. This class, thus, provides
 * the REST API entry point into the VAP SaaS Application.
 */
@RestController
@RequestMapping(VapServiceConstants.INFRA_MANAGEMENT_URL)
@ResponseBody
@Import(IntegrationConfig.class)
public class InfraHookResource extends BaseResource<InfraHookController> {
    @Value("${spring.profiles.active}")
    private String profile;
    @Value("${symphony.auth.csp-uri:http://localhost:8000}")
    private String cspURI;
    @Value("${symphony.auth.service-auth-id:vapservice}")
    private String cspServiceAuthID;
    @Value("${symphony.auth.service-auth-secret:vapservice}")
    private String cspAuthSecret;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private IntegrationConfig integrationConfig;

    private Logger logger = LoggerFactory.getLogger(InfraHookResource.class);



    @RequestMapping()
    @ResponseBody
    public String defaultMethod() {
        return "DEFAULT HANDLING";
    }

    @RequestMapping("*")
    @ResponseBody
    public String fallbackMethod() {
        return "fallback method";
    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        this.getC().createActors();
        logger.info("Started application with profile " + profile + " and csp pointing to uri " + cspURI);
        logger.info("VAP Actors are created successfully.");
        if (integrationConfig.getServices().size() > 0) {
            VapServiceConstants.services = integrationConfig.getServices();
            VapServiceConstants.CSP_URI=cspURI;
            VapServiceConstants.VAP_CSP_AUTH_ID=cspServiceAuthID;
            VapServiceConstants.VAP_CSP_AUTH_SECRET=cspAuthSecret;
            logger.info("integration configs are set as system properties");
        }
    }

    @RequestMapping(value = "/getToken", method = RequestMethod.GET)
    @ResponseBody
    public CspAuthTokenResponse invokeCspForClientCredsToken(@RequestParam("service") String serviceName){
        ServiceConfig serviceConfig = getService(serviceName);
        CspAuthTokenResponse cspClientCredentials = getCspClientCredentials(
          serviceConfig.getAuth().get("cspAuthId"),
          serviceConfig.getAuth().get("cspAuthSecret"));

        return cspClientCredentials;

    }

    private ServiceConfig getService(String serviceName) {
        ServiceConfig serviceConfig=null;
        for (ServiceConfig service : integrationConfig.getServices()) {
            if(serviceName.equalsIgnoreCase(service.getName())){
                serviceConfig = service;
                break;
            }
        }
        return serviceConfig;
    }

    public CspAuthTokenResponse getCspClientCredentials(String cspServiceAuthID, String cspAuthSecret) {
        String cspUri = this.cspURI;
        if (cspServiceAuthID != null && cspAuthSecret != null && cspUri != null) {
            HttpHeaders headers = new HttpHeaders();
            String encoded = Base64Utils
              .encodeToString(String.format("%s:%s", cspServiceAuthID, cspAuthSecret).getBytes(StandardCharsets.UTF_8));
            headers.add("Authorization", String.format("Basic %s", encoded));
            headers.add("Content-Type", "application/x-www-form-urlencoded");
            HttpEntity<String> entity = new HttpEntity("grant_type=client_credentials", headers);
            String url = String.format("%s/csp/gateway/am/api/login/oauth", cspUri);
            return (CspAuthTokenResponse)this.restTemplate.postForObject(url, entity, CspAuthTokenResponse.class, new Object[0]);
        } else {
            throw new CspSystemException(String.format("clientId %s, clientSecret %s and cspUri %s cannot be null", cspServiceAuthID, cspAuthSecret, cspUri));
        }
    }
}
