package com.vmware.apps.vap.akka;

import akka.actor.Props;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vmware.akka.common.BaseVapActorWithTimer;
import com.vmware.apps.vap.akka.dto.ServiceRegistryDto;
import com.vmware.common.constants.VapServiceConstants;
import com.vmware.common.utils.PrintUtils;
import com.vmware.ignite.common.RegistryManager;
import com.vmware.ignite.util.JobType;
import com.vmware.apps.vap.ignite.ServiceRegistry;
import com.vmware.vap.bridge.common.VAPBridgeConstants;
import com.vmware.vap.service.VapServiceUtils;
import com.vmware.vap.service.control.OnPremVapDelegate;
import com.vmware.vap.service.dto.CallbackMessageDTO;
import com.vmware.vap.service.dto.EndpointStateEnum;
import com.vmware.vap.service.dto.PluginManagementResponse;
import com.vmware.vap.service.dto.PluginsDTO;
import com.vmware.vap.service.exception.LemansServiceException;
import com.vmware.vap.service.exception.RegistryException;
import com.vmware.vap.service.message.ActorMessage;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import state.PluginState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import static com.vmware.common.constants.VapServiceConstants.*;

/**
 * PluginActor to handle the plugin related behaviors. See
 * {@link com.vmware.vap.service.control.VapActorType.PLUGIN}
 * for more information.
 */
public class PluginActor extends BaseVapActorWithTimer {
    private OnPremVapDelegate onPremVapDelegate;
    private static Logger logger = LoggerFactory.getLogger(PluginActor.class);


    private PluginActor(RegistryManager rm,
                        OnPremVapDelegate onPremVapDelegate) {
        super(rm);
        this.onPremVapDelegate = onPremVapDelegate;
    }

    static public Props props(RegistryManager rm,
                              OnPremVapDelegate onPremVapDelegate) {
        return Props.create(PluginActor.class, rm, onPremVapDelegate);
    }

    @Override
    public Receive createReceive() {
        return super.createReceive().orElse(receiveBuilder()
          .match(PluginActionsMessage.class, this::handlePluginsActionMessage)
          .match(HandleConfiguredPluginsMessage.class,
            this::handleConfiguredPluginsMessage).match(
            HandlePluginManagementMessage.class,
            this::handlePluginsManagementMessage).build());
    }

    /**
     * Handles messages received to perform plugins related actions. Refer {@link
     * com.vmware.vap.service.control.VapActorType.PLUGIN} for more information. 1. Plugin activation 2.
     * Plugin deactivation
     */
    private void handlePluginsActionMessage(
        PluginActionsMessage pluginActionsMessage) {

        ServiceRegistry serviceReg = this.getServiceReg();

        ServiceRegistry.DService service = serviceReg.getServiceForVm(
            pluginActionsMessage.pluginsDTO.getItemID(),
            pluginActionsMessage.pluginsDTO.getPluginName());

        ArrayList<String> endpoints = new ArrayList<>();
        endpoints.add(pluginActionsMessage.vmID);

        if (PluginsDTO.State.enable
            .equals(pluginActionsMessage.pluginsDTO.getState())) {
            // we expect services to be created in registry from service
            // discovery message callback
            if (service == null) {
                logger.error("Unable to find service {} for the action {}",
                  pluginActionsMessage.pluginsDTO.getPluginName(),
                  pluginActionsMessage.pluginsDTO.getState());
                //service = createService(pluginActionsMessage, serviceReg);
            }
            try {
                if (pluginActionsMessage.pluginsDTO.getPluginInfo() != null) {
                    serviceReg.updateServiceConfig(service.getKey(),
                        pluginActionsMessage.pluginsDTO.getPluginInfo());
                }
                serviceReg.updateServiceState(service.getKey(),
                    EndpointStateEnum.INITIALIZED);

                getOnPremVapDelegate()
                    .managePlugins(pluginActionsMessage.getRequestID(),
                        pluginActionsMessage.pluginsDTO);

                JobStatusTick jobStatusTick = initializeJobWithPeriodicCheck(
                  pluginActionsMessage.pluginsDTO.getCloudProxyId(),
                  pluginActionsMessage.requestID,
                  JobType.PLUGIN_MANAGEMENT,
                  pluginActionsMessage.getJobData(),
                  120,
                  endpoints);

                jobTicksForReqMap
                  .put(pluginActionsMessage.requestID, jobStatusTick);

                getServiceReg().updateServiceState(service.getKey(),
                    EndpointStateEnum.ENABLE_PROGRESS);
            } catch (LemansServiceException ex) {
                logger.error(ex.getMessage(), ex);
                updateStateOnLemansError(service);
            } catch (RegistryException ex) {
                logger.error(ex.getMessage(), ex);
            }
        } else if (pluginActionsMessage.pluginsDTO.getState()
            .equals(PluginsDTO.State.disable)) {
            if (service == null) {
                return;
            }
            try {
                getOnPremVapDelegate()
                    .managePlugins(pluginActionsMessage.getRequestID(),
                        pluginActionsMessage.pluginsDTO);

                JobStatusTick jobStatusTick = initializeJobWithPeriodicCheck(
                  pluginActionsMessage.pluginsDTO.getCloudProxyId(),
                  pluginActionsMessage.requestID,
                  JobType.PLUGIN_MANAGEMENT,
                  pluginActionsMessage.getJobData(),
                  120,
                  endpoints);

                jobTicksForReqMap
                  .put(pluginActionsMessage.requestID, jobStatusTick);

                getServiceReg().updateServiceState(service.getKey(),
                    EndpointStateEnum.DISABLE_PROGRESS);
            } catch (LemansServiceException ex) {
                logger.error(ex.getMessage(), ex);
                updateStateOnLemansError(service);
            } catch (RegistryException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private ServiceRegistry.DService createService(
      PluginActionsMessage pluginActionsMessage, ServiceRegistry serviceReg) {
        ServiceRegistry.DService service;
        ServiceRegistryDto serviceRegistryDto = new ServiceRegistryDto(
            pluginActionsMessage.pluginsDTO.getPluginName(),
            pluginActionsMessage.pluginsDTO.getItemID());
        PrintUtils.printToActor("adding service %s for vm %s",
            serviceRegistryDto.getServiceName(),
            serviceRegistryDto.getVmMOR());
        this.getServiceReg().add(serviceRegistryDto);
        service = serviceReg.getServiceForVm(
            pluginActionsMessage.pluginsDTO.getItemID(),
            pluginActionsMessage.pluginsDTO.getPluginName());
        return service;
    }

    private void updateStateOnLemansError(ServiceRegistry.DService service) {
        try {
            getServiceReg().updateServiceState(service.getKey(),
                EndpointStateEnum.LEMANS_ERROR);
        } catch (RegistryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Handles below call back messages. Refer {@link com.vmware.vap.service.control.VapActorType.PLUGIN} for
     * more information. 1. Plugin activation 2. Plugin deactivation
     */
    private void handlePluginsManagementMessage(
        HandlePluginManagementMessage message) {

        JsonObject serviceResponse = new Gson()
          .fromJson(message.getPayload(), JsonObject.class);
        if (!checkIfResponseIsValid(serviceResponse)) {
            PrintUtils.printToActor(String.format(
              "Did not receive a valid callback response for plugin action %s",
              message.getPayload()));
            return;
        }

        String pluginAction = serviceResponse.get(STATE).getAsString();
        String statusFromResponse = serviceResponse.get(STATUS).getAsString();
        String requestId = serviceResponse.get(VapServiceConstants.REQUEST_ID)
          .getAsString();
        String endpointId = serviceResponse.get(VAPBridgeConstants.ITEM_ID)
          .getAsString();
        String pluginName = serviceResponse.get(PLUGINNAME).getAsString();

        ServiceRegistry serviceRegistry = this.getServiceReg();

        ServiceRegistry.DService serviceRegEntry = serviceRegistry
          .getServiceForVm(endpointId, pluginName);


        try {

            switch (statusFromResponse) {
            case "FINISHED":
                if (PluginState.StateEnum.ENABLE.getValue()
                  .equalsIgnoreCase(pluginAction)) {
                    serviceRegistry.updateServiceState(serviceRegEntry.getKey(),
                      EndpointStateEnum.ENABLE);
                } else {
                    serviceRegistry.updateServiceState(serviceRegEntry.getKey(),
                      EndpointStateEnum.DISABLE);
                }
                updateJobTicker(requestId, endpointId, statusFromResponse);
                break;
            case "FAILED":
                serviceRegistry.updateServiceState(serviceRegEntry.getKey(),
                  EndpointStateEnum.ERROR);
                updateJobTicker(requestId, endpointId, statusFromResponse);
                break;

            }
        } catch (RegistryException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    private void updateJobTicker(String requestId, String endpointId,
      String jobStatus) {

        UUID requestIdVal = UUID.fromString(requestId);
        if (jobTicksForReqMap.containsKey(requestIdVal)) {
            JobStatusTick jobStatusTick = jobTicksForReqMap.get(requestIdVal);
            Map<String, String> endpointMap = new HashMap<>();
            endpointMap.put(endpointId, jobStatus);

            updateJobWithEndpointStatus(jobStatusTick,
              JobType.PLUGIN_MANAGEMENT,
              jobStatus,
              endpointMap);

            jobTicksForReqMap.remove(requestId);
        }
    }

    /**
     * Handles below call back messages. Refer {@link com.vmware.vap.service.control.VapActorType.PLUGIN} for
     * more information. 1. Configured Plugins
     */
    private void handleConfiguredPluginsMessage(
        HandleConfiguredPluginsMessage message) {
        ServiceRegistry serviceReg = getServiceReg();
        for (String configuredPluginMessage : message.getPayload()
            .split("\n")) {
            JSONObject configuredPluginJSON = new JSONObject(
                configuredPluginMessage);
            //Configured plugin wil always have one item.
            Iterator<String> iterator = configuredPluginJSON.keySet()
                .iterator();
            if (iterator.hasNext()) {
                String configurePluginKey = iterator.next();
                JSONObject pluginConfigJSON = configuredPluginJSON
                    .getJSONObject(configurePluginKey);

                Map<String, String> pluginDetailsMap = new HashMap<>();
                for (String key : pluginConfigJSON.keySet()) {
                    pluginDetailsMap.put(key, pluginConfigJSON.getString(key));
                }

                String vcUUID = pluginDetailsMap
                    .remove(VAPBridgeConstants.VC_UUID);
                String vmMOR = pluginDetailsMap
                    .remove(VAPBridgeConstants.VM_MOR);

                String configuredPlugin = configurePluginKey.split("_")[0];
                ServiceRegistry.DService serviceForVm = serviceReg
                    .getServiceForVm(VapServiceUtils
                        .contructEndpointID(vcUUID, vmMOR), configuredPlugin);
                if (serviceForVm == null) {
                    logger.error("Not able to find the service : {} ",
                      configuredPlugin);
                    continue;
                }
                try {
                    serviceReg
                      .updateServiceConfigWithKeys(serviceForVm.getKey(),
                        pluginDetailsMap);
                } catch (RegistryException e) {
                    logger.error(e.getMessage(), e);
                }
            } else {
                logger.error(
                    "Configured plugin message received without any configured plugin key");
            }
        }
    }

    private boolean checkIfResponseIsValid(JsonObject serviceResponse) {

        return (serviceResponse.get(VAPBridgeConstants.ITEM_ID) != null
          && serviceResponse.get(STATE) != null
          && serviceResponse.get(STATUS) != null
          && serviceResponse.get(PLUGINNAME) != null)
          && serviceResponse.get(REQUEST_ID) != null;
    }

    @Override
    protected void checkJobStatus(JobStatusTick jobStatusTick) {
        UUID requestId = jobStatusTick.getKey().getRequestID();
        String agentId = jobStatusTick.getKey().getCloudProxyID();

        try {
            PluginManagementResponse pluginManagementResponse = onPremVapDelegate
              .checkJobStatus(requestId, agentId, JobType.PLUGIN_MANAGEMENT);
            if (pluginManagementResponse.getResultStatus() == null) {
                return;
            }
            StringBuilder jobState = new StringBuilder(pluginManagementResponse
              .getResultStatus());

            if (pluginManagementResponse.getCommandResult().getErrorCode()
              != 0) {
                jobState.append(" - ")
                  .append(pluginManagementResponse.getCommandResult()
                    .getErrorMessage());
            }

            Map<String, String> endpointStatus = new HashMap<>();
            endpointStatus.put(
              pluginManagementResponse.getItemID(),
              pluginManagementResponse.getResultStatus());

            updateJobWithEndpointStatus(
              jobStatusTick,
              JobType.PLUGIN_MANAGEMENT,
              jobState.toString(),
              endpointStatus);

            jobTicksForReqMap.remove(requestId);

        } catch (LemansServiceException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    private void updateJobStatus() {

    }

    public static class HandlePluginManagementMessage extends ActorMessage {
        public HandlePluginManagementMessage(CallbackMessageDTO messageDTO) {
            super(messageDTO);
        }
    }

    public static class HandleConfiguredPluginsMessage extends ActorMessage {
        public HandleConfiguredPluginsMessage(CallbackMessageDTO messageDTO) {
            super(messageDTO);
        }
    }

    private OnPremVapDelegate getOnPremVapDelegate() {

        return this.onPremVapDelegate;
    }

    private ServiceRegistry getServiceReg() {

        return reg(ServiceRegistry.class);
    }

    public static class PluginActionsMessage {
        String vmID;
        PluginsDTO pluginsDTO;
        private UUID requestID;

        public PluginActionsMessage(UUID requestID, PluginsDTO pluginsDTO) {

            this.vmID = pluginsDTO.getItemID();
            this.pluginsDTO = pluginsDTO;
            this.requestID = requestID;
        }

        public String getVmID() {
            return vmID;
        }

        public PluginsDTO getPluginsDTO() {
            return pluginsDTO;
        }

        public UUID getRequestID() {
            return requestID;
        }

        public String getJobData() {
            pluginsDTO.getPluginInfo().getPlugin_config().remove("password");
            return VapServiceUtils.convertToJson(pluginsDTO, PluginsDTO.class);
        }
    }
}
