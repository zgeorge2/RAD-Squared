package com.vmware.apps.adm.ctrl;

import akka.actor.ActorRef;
import akka.routing.RoundRobinRoutingLogic;
import com.vmware.akka.router.MasterRouter;
import com.vmware.akka.router.WorkerClassArgs;
import com.vmware.apps.adm.akka.NodeAdmin;
import com.vmware.apps.bank.akka.Printer;
import com.vmware.ctrl.ControllerDependency;
import com.vmware.ignite.common.RegistryManager;

import java.util.function.Consumer;

/**
 * Used to initialize an intial set of actors needed by the Admin App
 */
public class AdmAppInitializer implements ControllerDependency {
    public AdmAppInitializer(RegistryManager rm) {
        // add a node admin Actor
        createNodeAdmin(rm);
        // add a couple of printers with a router
        createPrinters(rm);
        // add a shutdown hook
        rm.getAU().setupActorSystemShutdown();
    }

    private void createPrinters(RegistryManager rm) {
        Consumer<WorkerClassArgs> wargsSupplier = (args) -> {
            args.put(Printer.BANNER_KEY, "PRINTER_BANNER");
        };
        rm.getAU().add(() -> MasterRouter.props(rm, new RoundRobinRoutingLogic(), 5,
            Printer::props, wargsSupplier), Printer.PRINTER_MASTER_ROUTER_NAME)
            .tell(new MasterRouter.Initialize(), ActorRef.noSender());
    }

    private void createNodeAdmin(RegistryManager rm) {
        rm.getAU().add(() -> NodeAdmin.props(rm), NodeAdmin.NODE_ADMIN_NAME);
    }
}

