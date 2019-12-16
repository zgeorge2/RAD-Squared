package com.vmware.sb.apps.vap;

import com.vmware.common.constants.VapServiceConstants;
import com.vmware.apps.vap.ctrl.VAPAPIController;
import com.vmware.sb.res.BaseResource;
import com.vmware.vap.service.dto.EndpointDTO;
import com.vmware.vap.service.dto.EndpointSearchRequestDTO;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(VapServiceConstants.SEARCH_URL)
@ResponseBody
@Api(tags = "VAP Get Operations", value="VAP Get Operations" , description = "Operation to get the managed endpoints ")
public class VAPAPIResource extends BaseResource<VAPAPIController> {

    @RequestMapping(value = VapServiceConstants.MANAGE_ENDPOINTS_PREFIX, method = RequestMethod.POST)
    public List<EndpointDTO> getManagedEndpoints(@RequestBody
                                                             EndpointSearchRequestDTO endpointSearchRequestDTO) {
        return this.getC().getManagedEndpoints(endpointSearchRequestDTO);
    }

}
