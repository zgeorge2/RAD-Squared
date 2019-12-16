package com.vmware.sb.apps.vap;

import com.vmware.common.constants.VapServiceConstants;
import com.vmware.apps.vap.ctrl.PluginController;
import com.vmware.sb.res.BaseResource;
import com.vmware.vap.service.dto.ManagePluginResponse;
import com.vmware.vap.service.dto.PluginsDTO;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * PluginResource
 */
@RestController
@RequestMapping(VapServiceConstants.PLUGIN_MANAGEMENT_URL)
@ResponseBody
@Api(tags = "VAP Plugin Management", value="VAP Plugin Management" , description = "Operation to "
            + "activate/deactivate agent plugin on endpoint")
public class PluginResource extends BaseResource<PluginController> {

    @RequestMapping(value = VapServiceConstants.ACTIVATE_PREFIX, method = RequestMethod.POST, produces = "application/json",
                consumes =
                "application/json")
    @ResponseBody
    public ManagePluginResponse activatePlugin(@RequestBody PluginsDTO pluginsDTO) {
        return this.getC().managePlugin(pluginsDTO);

    }

    @RequestMapping(value = VapServiceConstants.DEACTIVATE_PREFIX, method = RequestMethod.POST)
    @ResponseBody
    public ManagePluginResponse deactivatePlugin(@RequestBody PluginsDTO pluginsDTO) {
        return this.getC().managePlugin(pluginsDTO);
    }

}
