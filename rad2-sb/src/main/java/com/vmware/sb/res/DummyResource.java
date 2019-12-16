package com.vmware.sb.res;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.vmware.sb.apps.vap.VAPResource.PrintOut;

@RestController
@RequestMapping("/vapsaas")
public class DummyResource {


  @RequestMapping()
  public String defaultMethod() {
      return "DEFAULT HANDLING";
  }

  @RequestMapping("*")
  public String fallbackMethod() {
      return "fallback method";
  }
  @RequestMapping(value = "/hello", method = RequestMethod.GET)
  public PrintOut greet() {
      return new PrintOut("Hello VAP SAAS");
  }
}
