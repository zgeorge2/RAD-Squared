package com.vmware.vap.service.dto;


public class PluginsDTO {
  private String itemID;
  private String agentName;
  private String pluginName;
  private State state;
  private String lemansAgentId;
  private String cloudProxyId;
  private PluginInfo pluginInfo;
  private String documentSelfLink;


  public String getItemID() {
    return itemID;
  }

  public String getAgentName() {
    return agentName;
  }

  public String getPluginName() {
    return pluginName;
  }

  public PluginInfo getPluginInfo() {
    return pluginInfo;
  }

  public State getState() {
    return state;
  }

  public enum State {
    enable, disable;

    public String toString() {
      return this.name();
    }
  }

  public void setItemID(String itemID) {
    this.itemID = itemID;
  }

  public void setAgentName(String agentName) {
    this.agentName = agentName;
  }

  public void setPluginName(String pluginName) {
    this.pluginName = pluginName;
  }

  public void setState(State state) {
    this.state = state;
  }

  public void setPluginInfo(PluginInfo pluginInfo) {
    this.pluginInfo = pluginInfo;
  }

  public String getLemansAgentId() {
    return lemansAgentId;
  }

  public void setLemansAgentId(String lemansAgentId) {
    this.lemansAgentId = lemansAgentId;
  }

  public String getCloudProxyId() {
    return cloudProxyId;
  }

  public void setCloudProxyId(String cloudProxyId) {
    this.cloudProxyId = cloudProxyId;
  }

  public String getDocumentSelfLink() {
    return documentSelfLink;
  }

  public void setDocumentSelfLink(String documentSelfLink) {
    this.documentSelfLink = documentSelfLink;
  }
}
