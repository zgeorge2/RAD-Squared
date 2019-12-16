package com.vmware.vap.service.dto;

/**
 * ManagePluginResponse
 */
public class ManagePluginResponse {
  private String requestId;

  public ManagePluginResponse(String requestId) {
    this.requestId = requestId;
  }

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }
}
