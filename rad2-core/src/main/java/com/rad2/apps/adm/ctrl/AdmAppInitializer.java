package com.rad2.apps.adm.ctrl;

import akka.actor.ActorRef;
import akka.routing.RoundRobinRoutingLogic;
import com.rad2.akka.router.MasterRouter;
import com.rad2.akka.router.WorkerClassArgs;
import com.rad2.apps.adm.akka.JobTrackerAdmin;
import com.rad2.apps.adm.akka.JobTrackerWorker;
import com.rad2.apps.adm.akka.NodeAdmin;
import com.rad2.apps.bank.akka.Printer;
import com.rad2.ctrl.ControllerDependency;
import com.rad2.ignite.common.RegistryManager;

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
        // set up a job tracker admin to clean up state JobTrackerRegistry entries
        createJobTrackerAdmin(rm);
        // add a few job trackers
        createJobTrackers(rm);
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

    private void createJobTrackers(RegistryManager rm) {
        Consumer<WorkerClassArgs> wargsSupplier = (args) -> {
            args.put(JobTrackerWorker.BANNER_KEY, "JOB_TRACKER_BANNER");
        };
        rm.getAU().add(() -> MasterRouter.props(rm, new RoundRobinRoutingLogic(), 5,
                JobTrackerWorker::props, wargsSupplier), JobTrackerWorker.JOB_TRACKER_MASTER_ROUTER)
                .tell(new MasterRouter.Initialize(), ActorRef.noSender());
    }

    private void createNodeAdmin(RegistryManager rm) {
        rm.getAU().add(() -> NodeAdmin.props(rm), NodeAdmin.NODE_ADMIN_NAME);
    }

    private void createJobTrackerAdmin(RegistryManager rm) {
        rm.getAU().add(() -> JobTrackerAdmin.props(rm), JobTrackerAdmin.JT_ADMIN_NAME);
    }
}

