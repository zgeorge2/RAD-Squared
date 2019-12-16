package com.vmware.sb.apps.vap;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vmware.common.utils.PrintUtils;
import com.vmware.apps.vap.ctrl.VAPController;
import com.vmware.sb.auth.CSPAccessToken;
import com.vmware.sb.res.BaseResource;
import com.vmware.symphony.csp.auth.CspPropertyConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The VAPResource provides REST resources for use by external entities. This class, thus, provides the REST
 * API entry point into the VAP SaaS Application.
 */
@RestController
@RequestMapping("/vap")
public class VAPResource extends BaseResource<VAPController> {
    @Autowired
    CspPropertyConfiguration cspPropertyConfiguration;

    @RequestMapping()
    public String defaultMethod() {
        return "DEFAULT HANDLING";
    }

    @RequestMapping("*")
    public String fallbackMethod() {
        return "fallback method";
    }

    @RequestMapping(value = "/greeting/{pathvar}/{formvar}", method = RequestMethod.POST)
    public PrintOut greet(@PathVariable("pathvar") String pathvar,
                          @PathVariable("formvar") String formvar,
                          @RequestBody VCDTO banks) {
        PrintUtils.printToActor(banks.toString());
        return new PrintOut(this.getC().greeting(pathvar, formvar));
    }

    @RequestMapping(value = "/access-token", method = RequestMethod.GET)
    public CSPAccessToken getAuthToken(@CookieValue(value = "csp-auth-token",
        defaultValue = "") String cspAuthCookie, @RequestHeader(value =
        "authorization", defaultValue = "") String authorization,
                                       HttpServletResponse response) throws IOException {

        CSPAccessToken cspAccessToken = new CSPAccessToken();

        if (StringUtils.isNotEmpty(authorization) && authorization.startsWith
            ("Bearer")) {
            cspAccessToken.cspAuthToken = StringUtils.isNotEmpty(
                authorization.split(" ")[1]) ? authorization.split(" ")[1] : "";
        }

        if (StringUtils.isNotEmpty(cspAuthCookie)) {
            cspAccessToken.cspAuthToken = StringUtils.isNotEmpty(
                cspAuthCookie) ? cspAuthCookie : "";
        }

        if (StringUtils.isEmpty(cspAccessToken.cspAuthToken)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Could not fetch access token from request");
            response.addHeader("www-authenticate", "/vap/authn/callback");
            cspAccessToken.cspAuthToken = "Could not fetch access token from request";
            return cspAccessToken;
        } else {
            cspAccessToken.tokenType = "Bearer";
            return cspAccessToken;
        }
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

    public static class VCDTO {
        private List<VMDTO> vms;
        private String vcid;

        public VCDTO() {
            this.vms = new ArrayList<>();
        }

        @JsonProperty
        public List<VMDTO> getVms() {
            return vms;
        }

        @JsonProperty
        public String getVcid() {
            return vcid;
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(String.format("{VCID = %s", getVcid()));
            sb.append("[");
            this.getVms().forEach(x -> sb.append(x).append(","));
            sb.append("]}");
            return sb.toString();
        }

        public static class VMDTO {
            private String name;
            private String mor;

            @JsonProperty
            public String getName() {
                return name;
            }

            @JsonProperty
            public String getMOR() {
                return mor;
            }

            public String toString() {
                return String.format("{NAME:[%s], MOR:[%s]}", getName(), getMOR());
            }
        }
    }
}