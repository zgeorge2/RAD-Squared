package com.vmware.vap.service.dto;

import java.util.Map;

public class PluginInfo {

    public PluginInfo() {
    }

    public PluginInfo(Map<String, String> config, Map<String, String> identifiers) {
        this.plugin_config = config;
        this.plugin_identifier = identifiers;
    }

    private Map<String, String> plugin_identifier;
    private Map<String, String> plugin_config;

    public Map<String, String> getPlugin_identifier() {
        return plugin_identifier;
    }

    public void setPlugin_identifier(Map<String, String> plugin_identifier) {
        this.plugin_identifier = plugin_identifier;
    }

    public Map<String, String> getPlugin_config() {
        return plugin_config;
    }

    public void setPlugin_config(Map<String, String> plugin_config) {
        this.plugin_config = plugin_config;
    }
}
