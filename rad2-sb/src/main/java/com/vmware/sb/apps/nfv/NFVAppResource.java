package com.vmware.sb.apps.nfv;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vmware.apps.nfv.ctrl.NFVAppController;
import com.vmware.sb.res.BaseResource;
import org.springframework.web.bind.annotation.*;

/**
 * The NFVAppResource provides REST resources for use by external entities. This class, thus, provides the
 * REST API entry point into the NFV Application
 */
@RestController
@RequestMapping("/nfv/so")
public class NFVAppResource extends BaseResource<NFVAppController> {
    @RequestMapping()
    public String defaultMethod() {
        return "DEFAULT HANDLING";
    }

    @RequestMapping("*")
    public String fallbackMethod() {
        return "fallback method";
    }

    @PostMapping("/createOrder")
    public PrintOut createOrder(@RequestBody NFVAppController.CreateOrderDTO dto) {
        logger.info(String.format("POST /nfv/so/createOrder: %s", dto.getName()));
        this.getC().createOrder(dto);
        return new PrintOut(String.format("Create Order: /nfv/so/createOrder: %s", dto.getName()));
    }

    /**
     * REST END POINT FOR Network Slices
     */
    @PostMapping("/buySlices")
    public PrintOut buySlices(@RequestBody NFVAppController.BuyNetworkSlicesDTO dto) {
        logger.info("POST /nfv/so/buySlices ...");
        this.getC().buySlices(dto);
        return new PrintOut(String.format("Buying Slices:\n [%s]", dto));
    }

    @PostMapping("/returnSlices")
    public PrintOut returnSlices(@RequestBody NFVAppController.ReturnNetworkSlicesDTO dto) {
        logger.info("POST /nfv/so/returnSlices ...");
        this.getC().retSlices(dto);
        return new PrintOut(String.format("Returning Slices:\n [%s]", dto));
    }

    @GetMapping("/slices")
    public PrintOut listSlices() {
        logger.info("GET /nfv/so/slices ...");
        this.getC().listSlices();
        return new PrintOut("Listing Slices");
    }

    /**
     * REST END POINT FOR RESOURCE MANAGEMENT (NFV resources)
     */
    @PostMapping("/addResources")
    public PrintOut addResources(@RequestBody NFVAppController.ResourcesDTO dto) {
        logger.info("POST /nfv/so/addResources ...");
        this.getC().addResources(dto);
        return new PrintOut(String.format("Adding Resources:\n [%s]", dto));
    }

    @GetMapping("/resources")
    public PrintOut listResources() {
        logger.info("GET /nfv/so/resources ...");
        this.getC().listResources();
        return new PrintOut("Listing Resources");
    }

    @PostMapping("/resetAllResources")
    public PrintOut resetAllResources(@RequestBody NFVAppController.ResetAllResourcesDTO dto) {
        logger.info("POST /nfv/so/resetAllResources ...");
        this.getC().resetAllResources(dto);
        return new PrintOut(String.format("Reset All Resources:\n [%s]", dto));
    }

    @PostMapping("/reserveResourceSlices")
    public PrintOut reserveResourceSlices(@RequestBody NFVAppController.ReserveResourceSlicesDTO dto) {
        logger.info("POST /nfv/so/reserveResourceSlices ...");
        this.getC().reserveResourceSlices(dto);
        return new PrintOut(String.format("Reserving Resource Slices:\n [%s]", dto));
    }

    @PostMapping("/returnResourceSlices")
    public PrintOut returnResourceSlices(@RequestBody NFVAppController.ReturnResourceSlicesDTO dto) {
        logger.info("POST /nfv/so/returnResourceSlices ...");
        this.getC().returnResourceSlices(dto);
        return new PrintOut(String.format("Returning Resource Slices:\n [%s]", dto));
    }

    /**
     * REST END POINT FOR RELATIONSHIPS MANAGEMENT (NFV resource to resource relationships)
     */
    @PostMapping("/addRelationships")
    public PrintOut addRelationships(@RequestBody NFVAppController.RelsDTO dto) {
        logger.info("POST /nfv/so/addRelationships ...");
        this.getC().addRelationships(dto);
        return new PrintOut(String.format("Adding Relationships:\n [%s]", dto));
    }

    @GetMapping("/relationships")
    public PrintOut listRelationships() {
        logger.info("GET /nfv/so/relationships ...");
        this.getC().listRelationships();
        return new PrintOut("Listing Relationships");
    }

    @PostMapping("/resetAllRelationships")
    public PrintOut resetAllRelationships(@RequestBody NFVAppController.ResetAllRelsDTO dto) {
        logger.info("POST /nfv/so/resetAllRelationships ...");
        this.getC().resetAllRelationships(dto);
        return new PrintOut(String.format("Reset All Relationships:\n [%s]", dto));
    }

    @PostMapping("/reserveRelationshipSlices")
    public PrintOut reserveRelationshipSlices(@RequestBody NFVAppController.ReserveRelSlicesDTO dto) {
        logger.info("POST /nfv/so/reserveRelationshipSlices ...");
        this.getC().reserveRelationshipSlices(dto);
        return new PrintOut(String.format("Reserving Relationship Slices:\n [%s]", dto));
    }

    @PostMapping("/returnRelationshipSlices")
    public PrintOut returnRelationshipSlices(@RequestBody NFVAppController.ReturnRelSlicesDTO dto) {
        logger.info("POST /nfv/so/returnRelationshipSlices ...");
        this.getC().returnRelationshipSlices(dto);
        return new PrintOut(String.format("Returning Relationship Slices:\n [%s]", dto));
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