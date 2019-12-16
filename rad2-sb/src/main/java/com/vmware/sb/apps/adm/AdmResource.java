package com.vmware.sb.apps.adm;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vmware.apps.adm.ctrl.AdmController;
import com.vmware.sb.res.BaseResource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
/**
 * Administrative Resource end point provides some common controls applicable to ANY RAD-2 application
 */
@RestController
@RequestMapping("/adm")
public class AdmResource extends BaseResource<AdmController> {
    @PostMapping("/shutdown")
    public PrintOut shutdown() {
        this.getC().shutdown();
        return new PrintOut("RAD-2 Node Shutdown invoked ...");
    }
    @PostMapping("/increaseRoutees")
    public PrintOut increaseRoutees(@RequestBody AdmController.IncreaseRouteesDTO dto) {
        this.getC().increaseRoutees(dto);
        return new PrintOut(String.format("Increasing routees of [%s] in system [%s] ...",
                dto.getRouter(), dto.getSystem()));
    }
    @PostMapping("/removeRoutees")
    public PrintOut removeRoutees(@RequestBody AdmController.IncreaseRouteesDTO dto) {
        this.getC().removeeRoutees(dto);
        return new PrintOut(String.format("Removing routees of [%s] in system [%s] ...",
                dto.getRouter(), dto.getSystem()));
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