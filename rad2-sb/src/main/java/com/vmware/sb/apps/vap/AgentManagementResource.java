package com.vmware.sb.apps.vap;

import com.vmware.common.constants.VapServiceConstants;
import com.vmware.apps.vap.ctrl.AgentManagementController;
import com.vmware.sb.res.BaseResource;
import com.vmware.vap.service.dto.AgentManagementRequest;
import com.vmware.vap.service.dto.AgentManagementResponse;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(VapServiceConstants.AGENT_MANAGEMENT_URL)
@ResponseBody
@Api(tags = "Agent Management", value="VAP Agent Management" , description = "Operation to "
            + "start/stop/restart/content upgrade agent on endpoint")
public class AgentManagementResource extends BaseResource<AgentManagementController> {

    @RequestMapping(method=RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody
    public AgentManagementResponse defaultMethod(@RequestBody AgentManagementRequest agentManagementDTO) {
        return this.getC().manageAgent(agentManagementDTO);
    }
}