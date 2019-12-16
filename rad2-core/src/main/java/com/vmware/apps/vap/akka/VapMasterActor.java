package com.vmware.apps.vap.akka;

import akka.actor.ActorRef;
import akka.actor.Props;
import com.vmware.akka.common.BaseActor;
import com.vmware.common.utils.PrintUtils;
import com.vmware.vap.service.control.LemansOnPremVapDelegate;
import com.vmware.vap.service.control.OnPremVapDelegate;
import com.vmware.vap.service.control.VapActorType;
import com.vmware.ignite.common.RegistryManager;

/**
 * The master actor for VAP SaaS system. All other actors will be under the hierchy of this actor. There will be
 * multiple child actors per request type such as JobActor for Bootstrap & Plugin activation/deactivation and message
 * receiver types such as Bootstrap status, Discovered Services, Configured Plugins etc.
 *
 * Later these child actors can have pool of actors to act upon the ingest messages quickly.
 */
public class VapMasterActor  extends BaseActor {


    protected VapMasterActor(RegistryManager rm) {
        super(rm);
    }

    static public Props props(RegistryManager rm) {
        return Props.create(VapMasterActor.class, rm);
    }

    @Override
    public Receive createReceive() {
        return super.createReceive()
                .orElse(receiveBuilder()
                        // Messages to create actors
                        .match(Initialize.class, this::initialize)
                        .build());
    }

    protected void initialize(Initialize initializer) {
        ActorRef actorRef;
        // BootstrapActor

        OnPremVapDelegate onPremVapDelegate = new LemansOnPremVapDelegate(getRM());
        actorRef = this.add(()-> BootstrapActor.props(getRM(), onPremVapDelegate), VapActorType.BOOTSTRAP.getActorName());
        PrintUtils.printToActor("*** Added VAP Actor *** " + actorRef.path() + ", NAME = " + actorRef.path().name());

        // PluginActor
        actorRef = this.add(()->PluginActor.props(this.getRM(), onPremVapDelegate), VapActorType.PLUGIN.getActorName());
        PrintUtils.printToActor("*** Added VAP Actor *** " + actorRef.path() + ", NAME = " + actorRef.path().name());

        // ProcessesActor
        actorRef = this.add(()->ProcessesActor.props(this.getRM()), VapActorType.PROCESSES.getActorName());
        PrintUtils.printToActor("*** Added VAP Actor *** " + actorRef.path() + ", NAME = " + actorRef.path().name());

        // Fallback Actor
        actorRef = this.add(()->ProcessesActor.props(this.getRM()), VapActorType.FALLBACK.getActorName());
        PrintUtils.printToActor("*** Added VAP Actor *** " + actorRef.path() + ", NAME = " + actorRef.path().name());

        actorRef = this.add(() -> VapRetrieverActor.props(this.getRM()), VapActorType.RETRIEVER.getActorName());
        PrintUtils.printToActor("*** Added VAP Actor *** " + actorRef.path() + ", NAME = " + actorRef.path().name());

        actorRef = this.add(() -> AgentManagementActor.props(this.getRM(), onPremVapDelegate),
                    VapActorType.AGENT_MANAGEMENT.getActorName());
        PrintUtils.printToActor("*** Added VAP Actor *** " + actorRef.path() + ", NAME = " + actorRef.path().name());

        // Cloud Account
        actorRef = this.add(() -> CloudAccountActor.props(this.getRM()), VapActorType.CLOUD_ACCOUNT.getActorName());
        PrintUtils.printToActor("*** Added VAP Actor *** " + actorRef.path() + ", NAME = " + actorRef.path().name());

        // RDC State and Health
        actorRef = this.add(() -> RDCStateActor.props(this.getRM()), VapActorType.RDC_MANAGEMENT.getActorName());
        PrintUtils.printToActor("*** Added VAP Actor *** " + actorRef.path() + ", NAME = " + actorRef.path().name());
    }


    public static class Initialize {

    }

}
