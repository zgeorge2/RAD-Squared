/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.apps.finco.ctrl;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import com.rad2.akka.common.IDeferred;
import com.rad2.apps.finco.akka.FCAccountWorker;
import com.rad2.apps.finco.akka.FCData;
import com.rad2.apps.finco.akka.FinCoWorker;
import com.rad2.ctrl.BaseController;

import java.util.ArrayList;
import java.util.List;

/**
 * The FinCoController acts as the central brain of the application, it receives
 * instructions from the FinCoResource and uses the AkkaActorSystemUtility and
 * the Apache Ignite Registry to perform those instructions. The Akka Actor
 * System performs the asynchronous, distributed, concurrent programming. All
 * shared state across Actors are held within the Ignite registry and is
 * available to all Actors on any Node in the cluster.
 */
public class FinCoController extends BaseController {
    public void addFinCoList(FCData.FinCoList dto) {
        ActorSelection fcr = getFinCoRouter();
        fcr.tell(new FinCoWorker.AddFinCos(dto), ActorRef.noSender());
    }

    public void getAllBranches(IDeferred<String> req) {
        ActorSelection fcr = getFinCoRouter();
        fcr.tell(new FinCoWorker.GetAllBranches(req), ActorRef.noSender());
    }

    public void getAllAccountHolders(IDeferred<String> req) {
        ActorSelection fcr = getFinCoRouter();
        fcr.tell(new FinCoWorker.GetAllAccountHolders(req), ActorRef.noSender());
    }

    public void getAllAccounts(IDeferred<String> req) {
        ActorSelection fcr = getFinCoRouter();
        fcr.tell(new FinCoWorker.GetAllAccounts(req), ActorRef.noSender());
    }

    public void doTransfers(IDeferred<String> req) {
        ActorSelection fcar = getFCAccountRouter();
        fcar.tell(new FCAccountWorker.DoTransfers(req), ActorRef.noSender());
    }

    private ActorSelection getFinCoRouter() {
        return getAU().getActor(getAU().getLocalSystemName(), FinCoWorker.FINCO_MASTER_ROUTER);
    }

    private ActorSelection getFCAccountRouter() {
        return getAU().getActor(getAU().getLocalSystemName(), FCAccountWorker.FC_ACCOUNT_MASTER_ROUTER);
    }

    @Override
    public List<Class> getDependenciesList() {
        List<Class> ret = new ArrayList<>();
        ret.add(FinCoInitializer.class);
        return ret;
    }
}
