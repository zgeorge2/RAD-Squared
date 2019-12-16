package com.vmware.apps.nfv.ctrl;

import akka.actor.ActorRef;
import akka.routing.RoundRobinRoutingLogic;
import com.vmware.akka.router.MasterRouter;
import com.vmware.akka.router.WorkerClassArgs;
import com.vmware.apps.nfv.akka.NetworkSliceWorker;
import com.vmware.apps.nfv.akka.ProviderRelationshipWorker;
import com.vmware.apps.nfv.akka.ProviderResourceWorker;
import com.vmware.ctrl.ControllerDependency;
import com.vmware.ignite.common.RegistryManager;

import java.util.function.Consumer;

/**
 * Used to initialize an initial set of actors needed by the NFV App
 */
public class NFVAppInitializer implements ControllerDependency {
    public NFVAppInitializer(RegistryManager rm) {
        // add a couple of printers with a router
        createWorkers(rm);
    }

    private void createWorkers(RegistryManager rm) {
        Consumer<WorkerClassArgs> resWorkerArgs = (args) -> args.put(ProviderResourceWorker.BANNER_KEY,
            "DC_RESOURCE_BANNER");
        rm.getAU().add(() -> MasterRouter.props(rm, new RoundRobinRoutingLogic(), 5,
            ProviderResourceWorker::props, resWorkerArgs),
            ProviderResourceWorker.PROVIDER_RESOURCE_MASTER_ROUTER)
            .tell(new MasterRouter.Initialize(), ActorRef.noSender());

        Consumer<WorkerClassArgs> relWorkerArgs = (args) -> args.put(ProviderRelationshipWorker.BANNER_KEY,
            "DC_RELATIONSHIP_BANNER");
        rm.getAU().add(() -> MasterRouter.props(rm, new RoundRobinRoutingLogic(), 5,
            ProviderRelationshipWorker::props, relWorkerArgs),
            ProviderRelationshipWorker.PROVIDER_RELATIONSHIP_MASTER_ROUTER)
            .tell(new MasterRouter.Initialize(), ActorRef.noSender());

        Consumer<WorkerClassArgs> nsWorkerArgs = (args) -> args.put(NetworkSliceWorker.BANNER_KEY,
            "NETWORK_SLICE_BANNER");
        rm.getAU().add(() -> MasterRouter.props(rm, new RoundRobinRoutingLogic(), 5,
            NetworkSliceWorker::props, nsWorkerArgs),
            NetworkSliceWorker.NETWORK_SLICE_MASTER_ROUTER)
            .tell(new MasterRouter.Initialize(), ActorRef.noSender());
    }
}

