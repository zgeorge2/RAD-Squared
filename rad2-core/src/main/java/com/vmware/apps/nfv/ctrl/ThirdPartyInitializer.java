package com.vmware.apps.nfv.ctrl;

import akka.actor.ActorRef;
import akka.routing.RoundRobinRoutingLogic;
import com.vmware.akka.router.MasterRouter;
import com.vmware.akka.router.WorkerClassArgs;
import com.vmware.apps.nfv.akka.ThirdPartyWorker;
import com.vmware.ctrl.ControllerDependency;
import com.vmware.ignite.common.RegistryManager;

import java.util.function.Consumer;

/**
 * Used to initialize an initial set of actors needed by third party controller
 */
public class ThirdPartyInitializer implements ControllerDependency {
    public ThirdPartyInitializer(RegistryManager rm) {
        // add a couple of printers with a router
        createWorkers(rm);
    }

    private void createWorkers(RegistryManager rm) {
        Consumer<WorkerClassArgs> venWorkerArgs = (args) -> args.put(ThirdPartyWorker.BANNER_KEY,
            "THIRD_PARTY_VENDOR_BANNER");
        rm.getAU().add(() -> MasterRouter.props(rm, new RoundRobinRoutingLogic(), 5,
            ThirdPartyWorker::props, venWorkerArgs),
            ThirdPartyWorker.THIRD_PARTY_MASTER_ROUTER)
            .tell(new MasterRouter.Initialize(), ActorRef.noSender());
    }
}

