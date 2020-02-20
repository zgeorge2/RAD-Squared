package com.rad2.sb.apps.adm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rad2.apps.adm.ctrl.AdmController;
import com.rad2.sb.res.BaseResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * Administrative Resource end point provides some common controls applicable to ANY RAD-2 application
 */
@RestController
@RequestMapping("/adm")
public class AdmResource extends BaseResource<AdmController> {

    @GetMapping("/getJobResult/{pKey}/{name}")
    public DeferredResult<ResponseEntity<String>> getJobResult(@PathVariable String pKey, @PathVariable String name) {
        return retrieveJobResult(pKey, name);
    }

    @PostMapping("/shutdown")
    public PrintOut shutdown() {
        getC().shutdown();
        return new PrintOut("RAD-2 Node Shutdown invoked ...");
    }

    @PostMapping("/increaseRoutees")
    public PrintOut increaseRoutees(@RequestBody AdmController.UpdateRouteesDTO dto) {
        this.getC().increaseRoutees(dto);
        return new PrintOut(String.format("Increasing routees of [%s] in system [%s] ...",
                dto.getRouter(), dto.getSystem()));
    }

    @PostMapping("/removeRoutees")
    public PrintOut removeRoutees(@RequestBody AdmController.UpdateRouteesDTO dto) {
        this.getC().removeRoutees(dto);
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