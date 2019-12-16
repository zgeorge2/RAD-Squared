package com.vmware.apps.vap.ctrl;

import akka.actor.ActorRef;
import com.vmware.apps.vap.akka.VapMasterActor;
import com.vmware.common.utils.PrintUtils;
import com.vmware.vap.service.control.OnPremVapDelegate;
import com.vmware.vap.service.control.VapActorType;

import java.util.ArrayList;
import java.util.List;

public class InfraHookController extends BaseVAPController {

    public void createActors() {
        // Create a master actor. All other actors are under the supervision of this actor.
        ActorRef masterRef = getAU().add(() -> VapMasterActor.props(getRM()), VapActorType.MASTER.getActorName());
        PrintUtils.printToActor("*** Added VAP Master Actor *** "  + masterRef.path());

        // Create Child Actors
        masterRef.tell(new VapMasterActor.Initialize(), ActorRef.noSender());
    }

    private OnPremVapDelegate getVapDelegate() {
        return this.getDep(OnPremVapDelegate.class);
    }

    @Override
    public List<Class> getDependenciesList() {
        List<Class> ret = new ArrayList<>();
        ret.add(OnPremVapDelegate.class);
        return ret;
    }

    @Override
    protected VapActorType getActorType() {
        return null;
    }
}
