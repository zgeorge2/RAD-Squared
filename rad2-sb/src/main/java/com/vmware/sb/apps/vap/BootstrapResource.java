package com.vmware.sb.apps.vap;

import com.vmware.common.constants.VapServiceConstants;
import com.vmware.apps.vap.ctrl.BootstrapController;
import com.vmware.sb.res.BaseResource;
import com.vmware.vap.service.dto.AgentDeploymentRequest;
import com.vmware.vap.service.dto.BootstrapEndpointResponse;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * The VAPResource provides REST resources for use by external entities. This class, thus, provides the REST
 * API entry point into the VAP SaaS Application.
 */
@RestController
@RequestMapping(VapServiceConstants.AGENT_DEPLOYEMENT_URL)
@ResponseBody
@Api(tags = "Agent Deployment", value="VAP Agent Deployment" , description = "Operation to "
            + "install/uninstall agent on endpoint")
public class BootstrapResource extends BaseResource<BootstrapController> {

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public BootstrapEndpointResponse addManagedEndpoints(@RequestBody
                                                             AgentDeploymentRequest managedEndpoints) {
        return this.getC().bootstrapEndpoint(managedEndpoints);
    }
}
