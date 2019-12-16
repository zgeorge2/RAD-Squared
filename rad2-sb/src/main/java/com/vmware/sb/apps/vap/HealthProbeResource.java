package com.vmware.sb.apps.vap;

import io.swagger.annotations.Api;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vap-saas/api/vaphealth")
@Api(tags = "VAP Liveness Probe", value="VAP Liveness Probe" , description = "Operations to get the Health of VAP")
public class HealthProbeResource {

  @RequestMapping(value = "/health-alive", method = RequestMethod.GET)
  public ResponseEntity alivenessCheck() {
    return ResponseEntity.ok().build();
  }

  @RequestMapping(value = "/health-ready", method = RequestMethod.GET)
  public ResponseEntity readinessCheck() {
    return ResponseEntity.ok().build();
  }
}
