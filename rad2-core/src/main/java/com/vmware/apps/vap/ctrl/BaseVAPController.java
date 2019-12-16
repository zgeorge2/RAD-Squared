package com.vmware.apps.vap.ctrl;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import com.vmware.common.utils.PrintUtils;
import com.vmware.ctrl.BaseController;
import com.vmware.ctrl.deps.UUIDGenerator;
import com.vmware.ignite.common.SystemConfigRegistry;
import com.vmware.vap.service.control.VapActorType;
import com.vmware.vap.service.dto.CallbackMessageDTO;
import com.vmware.vap.service.message.CallbackToActorMessageConverter;

public abstract class BaseVAPController extends BaseController {
    protected ActorSelection getMasterActor() {
        return getAU().getActor(this.getLocalSystemName(), VapActorType.MASTER.getActorName());
    }

    protected ActorSelection getActor(VapActorType actorType) {
        return getAU().getActor(this.getLocalSystemName(), VapActorType.MASTER.getActorName(), actorType.getActorName());
    }

    public <T extends CallbackMessageDTO> void handleCallbackMessage(T callbackMessageDTO) {
        CallbackToActorMessageConverter converter = CallbackToActorMessageConverter.message2ActorMap.get(callbackMessageDTO
                .getMessageType());
        PrintUtils.printToActor("*** Found Actor Type *** " + converter.getActorType());

        this.getActor(converter.getActorType()).tell(converter.get(callbackMessageDTO), ActorRef.noSender());
    }

    protected String getVapRetriverActorName(){
        return "VapRetreiverActor_" ;
    }

    protected SystemConfigRegistry getSCReg() {
        return this.reg(SystemConfigRegistry.class);
    }

    protected String getLocalSystemName() {
        return this.getSCReg().getLocalSystemName();
    }

    protected ActorSelection getActor() {
        return getActor(getActorType());
    }

    protected UUIDGenerator getUUIDGen() {
        return this.getDep(UUIDGenerator.class);
    }

    protected abstract VapActorType getActorType();

}
