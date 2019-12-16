package com.vmware.apps.vap.ctrl;

import akka.actor.ActorRef;
import com.vmware.apps.vap.akka.PluginActor;
import com.vmware.vap.service.control.VapActorType;
import com.vmware.vap.service.dto.ManagePluginResponse;
import com.vmware.vap.service.dto.PluginsDTO;

import java.util.UUID;

/**
 * Created by nmadanapalli on 10/17/2018.
 */
public class PluginController extends BaseVAPController {

  @Override
  protected VapActorType getActorType() {
    return VapActorType.PLUGIN;
  }

  public ManagePluginResponse managePlugin(PluginsDTO pluginsDTO) {

    UUID requestID = getUUIDGen().generateNewUUID();

    this.getActor().tell(new PluginActor.PluginActionsMessage(requestID, pluginsDTO), ActorRef.noSender());

    return new ManagePluginResponse(requestID.toString());
  }


}
