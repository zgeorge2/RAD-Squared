package com.vmware.sb.apps.nfv;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vmware.apps.nfv.ctrl.ThirdPartyController;
import com.vmware.sb.res.BaseResource;
import org.springframework.web.bind.annotation.*;

/**
 * The ThirdPartyVendorResource provides REST resources for use by external entities. This class, thus,
 * provides the REST API entry point into the NFV Application's Third Party Vendor Management service
 */
@RestController
@RequestMapping("/nfv/tp")
public class ThirdPartyResource extends BaseResource<ThirdPartyController> {
    /**
     * REST END POINT FOR THIRD PARTY VENDOR MANAGEMENT
     */
    @PostMapping("/addVendors")
    public PrintOut addVendors(@RequestBody ThirdPartyController.AddVendorsDTO dto) {
        logger.info("POST /nfv/tp/addVendors ...");
        this.getC().addVendors(dto);
        return new PrintOut(String.format("Adding Vendors:\n [%s]", dto));
    }

    @GetMapping("/vendors")
    public PrintOut listVendors() {
        logger.info("GET /nfv/tp/vendors ...");
        this.getC().listVendors();
        return new PrintOut("Listing Vendors");
    }

    @PostMapping("/returnAllLicenses")
    public PrintOut returnAllLicenses(@RequestBody ThirdPartyController.ReturnAllLicensesDTO dto) {
        logger.info("POST /nfv/tp/returnAllLicenses ...");
        this.getC().returnAllLicenses(dto);
        return new PrintOut(String.format("Return all licenses:\n [%s]", dto));
    }

    @PostMapping("/buyLicenses")
    public PrintOut reserveResourceSlices(@RequestBody ThirdPartyController.BuyLicensesDTO dto) {
        logger.info("POST /nfv/tp/buyLicenses ...");
        this.getC().buyLicenses(dto);
        return new PrintOut(String.format("Buying licenses:\n [%s]", dto));
    }

    @PostMapping("/returnLicenses")
    public PrintOut returnLicenses(@RequestBody ThirdPartyController.ReturnLicensesDTO dto) {
        logger.info("POST /nfv/tp/returnLicenses ...");
        this.getC().returnLicenses(dto);
        return new PrintOut(String.format("Returning licenses:\n [%s]", dto));
    }

    @GetMapping("/functionRequests")
    public PrintOut functionRequests() {
        logger.info("GET /nfv/tp/functionRequests ...");
        this.getC().listFunctionRequests();
        return new PrintOut("Listing Function Requests");
    }

    @PostMapping("/buyFunctionRequests")
    public PrintOut buyFunctionRequests(@RequestBody ThirdPartyController.BuyFunctionRequestsDTO dto) {
        logger.info("POST /nfv/tp/buyFunctionRequests ...");
        this.getC().buyFunctionRequests(dto);
        return new PrintOut(String.format("Buying Function Requests:\n [%s]", dto));
    }

    /**
     * Classes below are used for Jackson based JSON->Obj-JSON transforms in some of the above resources
     */
    public static class PrintOut {
        private String value;

        public PrintOut(String value) {
            this.value = value;
        }

        @JsonProperty
        public String getValue() {
            return value;
        }
    }
}