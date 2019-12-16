package com.vmware.vap.service.dto;

import com.vmware.xenon.services.common.TaskService;
import state.CommandResult;

import java.util.Map;

public class PluginManagementResponse {

    String itemID;
    String agentName;
    String pluginName;
    PluginsDTO.State state;
    CommandResult commandResult;
    String resultStatus;

    public CommandResult getCommandResult() {
        return commandResult;
    }

    public void setCommandResult(CommandResult commandResult) {
        this.commandResult = commandResult;
    }

    public String getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public PluginsDTO.State getState() {
        return state;
    }

    public void setState(PluginsDTO.State state) {
        this.state = state;
    }
}
