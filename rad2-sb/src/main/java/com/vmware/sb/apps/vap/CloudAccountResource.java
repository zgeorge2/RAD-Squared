package com.vmware.sb.apps.vap;

import com.vmware.common.constants.VapServiceConstants;
import com.vmware.apps.vap.ctrl.CloudAccountController;
import com.vmware.sb.res.BaseResource;
import com.vmware.vap.service.dto.CloudAccountToVAPMappingRequest;
import com.vmware.vap.service.dto.ValidateCloudAccountRequest;
import com.vmware.vap.service.dto.ValidateCloudAccountResponse;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The CloudAccountResource provides REST resources for use by internal entities such as UI to validate cloud
 * account getting added into the system, thereby the account is used to bootstrap endpoint VMs.
 */
@RestController
@RequestMapping(VapServiceConstants.CLOUD_MANAGEMENT_URL)
@ResponseBody
@Api(tags = "Manage Cloud Account", value = "Manage Cloud Account", description = "Manage the cloud account")
public class CloudAccountResource extends BaseResource<CloudAccountController> {
    @RequestMapping(value = VapServiceConstants.VALIDATE_JOB_PREFIX, method = RequestMethod.POST)
    @ResponseBody
    public ValidateCloudAccountResponse validateCloudAccount(@RequestBody ValidateCloudAccountRequest accountDTO) {
        return this.getC().validateCloudAccount(accountDTO);
    }

    @RequestMapping(method = RequestMethod.POST)
    public boolean mapCloudAccountToVAP(@RequestBody CloudAccountToVAPMappingRequest vapToCloudAccountMapperRequest) {
        return this.getC().mapCloudAccount(vapToCloudAccountMapperRequest);
    }

    @RequestMapping(method = RequestMethod.GET)
    //TODO return type will change once we integrate with discovery service
    public List<CloudAccountToVAPMappingRequest> getAll() {
        return this.getC().getAll();
    }

    /**
     * one vap instance can manage multiple cloud accounts.
     *
     * @return All the cloud accounts, mappedVAPDcId is managing.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public CloudAccountToVAPMappingRequest get(@PathVariable("id") String cloudAccountId) {
        return this.getC().get(cloudAccountId);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public boolean unMapCloudAccount(@PathVariable("id") String cloudAccountId) {
        //TODO
        return false;
    }
}
