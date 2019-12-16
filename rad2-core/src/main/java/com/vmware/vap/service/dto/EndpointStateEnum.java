package com.vmware.vap.service.dto;

import com.google.gson.annotations.SerializedName;

public enum EndpointStateEnum {

  @SerializedName("enable")
  ENABLE("enable"),

  @SerializedName("disable")
  DISABLE("disable"),

  @SerializedName("initialized")
  INITIALIZED("initialized"),

  @SerializedName("configured")
  CONFIGURED("configured"),

  @SerializedName("inprogress")
  INPROGRESS("inprogress"),

  @SerializedName("enableprogress")
  ENABLE_PROGRESS("enableprogress"),

  @SerializedName("disableprogress")
  DISABLE_PROGRESS("disableprogress"),

  @SerializedName("error")
  ERROR("error"),

  @SerializedName("lemanserror")
  LEMANS_ERROR("lemanserror"),

  @SerializedName("pluginerror")
  PLUGIN_ERROR("pluginerror"),

  @SerializedName("notconfigured")
  NOTCONFIGURED("notconfigured");

  private String value;

  EndpointStateEnum(String value){
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
