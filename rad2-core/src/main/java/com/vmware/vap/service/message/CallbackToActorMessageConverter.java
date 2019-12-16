package com.vmware.vap.service.message;

import com.vmware.apps.vap.akka.*;
import com.vmware.vap.bridge.common.MessageType;
import com.vmware.vap.service.control.VapActorType;
import com.vmware.vap.service.dto.CallbackMessageDTO;

import java.util.HashMap;
import java.util.function.Function;

/**
 * Converts the callback message to actor specific message.
 */
public class CallbackToActorMessageConverter<R extends ActorMessage> {
    private VapActorType actorType;
    private Function<CallbackMessageDTO, R> function;

    public CallbackToActorMessageConverter(
        VapActorType actorType,
        Function<CallbackMessageDTO, R> function) {
        this.actorType = actorType;
        this.function = function;
    }

    public VapActorType getActorType() {
        return actorType;
    }

    public R get(CallbackMessageDTO callbackMessageDTO) {
        return this.function.apply(callbackMessageDTO);
    }

    public static HashMap<MessageType, CallbackToActorMessageConverter> message2ActorMap;

    static {
        message2ActorMap = new HashMap<>();

        message2ActorMap.put(MessageType.CONTROL_PLANE_ACTION_BOOTSTRAP,
            new CallbackToActorMessageConverter<>(
                VapActorType.BOOTSTRAP,
                (callbackMessageDTO) -> new BootstrapActor.HandleBootstrapMessage(
                    callbackMessageDTO)));

        message2ActorMap.put(MessageType.CONTROL_PLANE_ACTION_PLUGIN_MANAGEMENT,
            new CallbackToActorMessageConverter<>(VapActorType.PLUGIN,
                (callbackMessageDTO) -> new PluginActor.HandlePluginManagementMessage(
                    callbackMessageDTO)));

        message2ActorMap.put(
          MessageType.CONTROL_PLANE_ACTION_AGENT_MANAGEMENT,
          new CallbackToActorMessageConverter<>(
            VapActorType.AGENT_MANAGEMENT,
            (callbackMessageDTO) -> new AgentManagementActor.HandleAgtMgmtCallBackMsg(
              callbackMessageDTO)));

        // TODO: Change the line below to actor specific message DTO
        message2ActorMap.put(MessageType.VAP_STATE_HEALTH,
            new CallbackToActorMessageConverter<>(VapActorType.RDC_MANAGEMENT,
                RDCStateActor.RDCHealth::new));

        // TODO: Change the line below to actor specific message DTO
        message2ActorMap.put(MessageType.ENDPOINT_STATE_MEPS_HEALTH,
            new CallbackToActorMessageConverter<>(
                VapActorType.AGENT_MANAGEMENT,
                AgentManagementActor.Meps::new));

        message2ActorMap.put(MessageType.ENDPOINT_STATE_CONFIGURED_PLUGINS,
            new CallbackToActorMessageConverter<>(VapActorType.PLUGIN,
                (callbackMessageDTO) -> new PluginActor.HandleConfiguredPluginsMessage(
                    callbackMessageDTO)));

        message2ActorMap.put(MessageType.DATA_PLANE_PROCESSES,
            new CallbackToActorMessageConverter<>(
                VapActorType.PROCESSES,
                ProcessesActor.HandleProcessesMessage::new));
    }
}
