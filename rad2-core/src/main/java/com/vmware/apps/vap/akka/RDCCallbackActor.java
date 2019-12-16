package com.vmware.apps.vap.akka;

import akka.actor.Props;
import com.vmware.akka.common.BaseActor;
import com.vmware.common.utils.PrintUtils;
import com.vmware.ignite.common.RegistryManager;
import com.vmware.vap.service.dto.CallbackMessageDTO;
import com.vmware.vap.service.message.ActorMessage;


/**
 * This is a fallback actor to handle message of unimplemented message types. This call will be deleted once all the
 * message handling is done
 */
public class RDCCallbackActor extends BaseActor {
    protected RDCCallbackActor(RegistryManager rm) {
        super(rm);
    }

    static public Props props(RegistryManager rm) {
        return Props.create(RDCCallbackActor.class, rm);
    }

    @Override
    public Receive createReceive() {
        return super.createReceive()
            .orElse(receiveBuilder()
                .match(RDCCallbackMessage.class, this::handleCallbackMessage)
                .build());
    }

    protected void handleCallbackMessage(RDCCallbackMessage message) {
        PrintUtils.printToActor(String.format("Received callback message in fallback actor message type: %s",
                message.getMessageType().toString()));
    }


    public static class RDCCallbackMessage extends ActorMessage {
        public RDCCallbackMessage(CallbackMessageDTO messageDTO) {
            super(messageDTO);
        }
    }

}
