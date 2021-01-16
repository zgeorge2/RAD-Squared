/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.apps.finco.ctrl;

import akka.actor.ActorRef;
import akka.routing.RoundRobinRoutingLogic;
import com.rad2.akka.router.MasterRouter;
import com.rad2.akka.router.WorkerClassArgs;
import com.rad2.apps.finco.akka.FinCoWorker;
import com.rad2.ctrl.ControllerDependency;
import com.rad2.ignite.common.RegistryManager;

import java.util.function.Consumer;

/**
 * Used to initialize an intial set of actors needed by the FinCo App
 */
public class FinCoInitializer implements ControllerDependency {
    public FinCoInitializer(RegistryManager rm) {
        createWorkers(rm);
    }

    private void createWorkers(RegistryManager rm) {
        Consumer<WorkerClassArgs> workerArgs =
                (args) -> args.put(FinCoWorker.BANNER_KEY, "FINCO_BANNER");
        rm.getAU().add(() -> MasterRouter.props(rm, new RoundRobinRoutingLogic(), 5,
                FinCoWorker::props, workerArgs), FinCoWorker.FINCO_MASTER_ROUTER)
                .tell(new MasterRouter.Initialize(), ActorRef.noSender());
    }
}
