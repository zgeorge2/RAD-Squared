/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.apps.finco.akka;

import akka.actor.ActorSelection;
import akka.actor.Props;
import com.rad2.akka.aspects.ActorMessageHandler;
import com.rad2.akka.common.BaseActorWithTimer;
import com.rad2.apps.finco.ignite.FCAccountRegistry;
import com.rad2.common.utils.PrintUtils;
import com.rad2.ctrl.deps.IJobRef;
import com.rad2.ignite.common.RegistryManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A MoneyTransfer Actor is always created to coordinate a list of transfers.
 * This decouples the handling logic from the accounts. This Actor then
 * coordinates the transfer from one account to another for each transfer in the
 * list. If need be, TPC protocols can be introduced here.
 */
public class FCMoneyTransfer extends BaseActorWithTimer {
    private static final long TICK_TIME = 1000; // unit: millis
    private IJobRef jr; // to track ALL the operations done by this MT
    private final List<FCData.FCTransfer> transfers;
    private int transferCount;
    private final Map<String, Boolean> debits; // status of all debits
    private final Map<String, Boolean> credits; // status of all credits

    public FCMoneyTransfer(RegistryManager rm, IJobRef jr, List<FCData.FCTransfer> transfers) {
        super(rm, new Tick(TickTypeEnum.PERIODIC, jr.regId(), TICK_TIME, TimeUnit.MILLISECONDS));
        this.jr = jr;
        this.transfers = transfers;
        this.transferCount = 0;
        this.debits = new HashMap<>();
        this.credits = new HashMap<>();
    }

    static public Props props(RegistryManager rm, IJobRef jr, List<FCData.FCTransfer> transfers) {
        return Props.create(FCMoneyTransfer.class, rm, jr, transfers);
    }

    private FCAccountRegistry getACCReg() {
        return reg(FCAccountRegistry.class);
    }

    private ActorSelection getFCAccountRouter() {
        return getAU().getActor(getAU().getLocalSystemName(), FCAccountWorker.FC_ACCOUNT_MASTER_ROUTER);
    }

    @Override
    public Receive createReceive() {
        return super.createReceive()
                .orElse(receiveBuilder()
                        .match(BeginTransfers.class, this::beginTransfers)
                        .match(DebitStatus.class, this::debitStatus)
                        .match(CreditStatus.class, this::creditStatus)
                        .match(Terminate.class, this::terminate)
                        .build());
    }

    @ActorMessageHandler
    private void beginTransfers(BeginTransfers arg) {
        transfers.forEach(this::transfer);
    }

    @ActorMessageHandler
    private void debitStatus(DebitStatus arg) {
        debits.put(arg.transferId, arg.status);
    }

    @ActorMessageHandler
    private void creditStatus(CreditStatus arg) {
        credits.put(arg.transferId, arg.status);
    }

    @ActorMessageHandler
    private void terminate(Terminate arg) {
        this.context().stop(self());
    }

    private void transfer(FCData.FCTransfer t) {
        String tid = getTid(t);
        if (t.getFrom() == null || t.getTo() == null) {
            PrintUtils.print("Cannot validate and complete transfer. Account setup is pending!");
            return; // the from/to accounts need to be non-null
        }
        // debit the fromAccount
        getFCAccountRouter().tell(new FCAccountWorker.Debit(tid, t, String.format("DEBITED FROM: %s", t.getFrom())), self());
        // credit the toAccount
        getFCAccountRouter().tell(new FCAccountWorker.Credit(tid, t, String.format("CREDITED TO: %s", t.getTo())), self());

        // this is the last use of this Actor - terminate it.
        //self().tell(new Terminate(), self());
    }

    private String getTid(FCData.FCTransfer t) {
        return String.format("FC_TRANS_%d/%d [%s] FROM [%s] TO [%s]",
                transferCount++, transfers.size(), t.getAmount(), t.getFrom(), t.getTo());
    }

    private boolean isReady(Tick tick) {
        boolean ret = true;
        for (FCData.FCTransfer t : transfers) {
            String tid = getTid(t);
            ret = ret && debits.get(tid);
            ret = ret && credits.get(tid);
        }
        return ret; // returns true iff ALL credits/debits statuses are true
    }

    @Override
    public void onTick(Tick t) {
        if (!isReady(t)) {
            return; // It hasn't reached. Hence, continue checking on the timer
        }
        // condition to stop is reached
        this.stopTimer(t);
        // get total status updated since job is done
        updateJobSuccess(jobRef(), "Completed Money Transfers");
        // terminate the actor as it has completed its task
        self().tell(new Terminate(), self());
    }

    private IJobRef jobRef() {
        return this.jr;
    }

    /**
     * Messages for the receive method above
     */
    static public class BeginTransfers {

    }

    static public class DebitStatus {
        String transferId;
        boolean status;

        public DebitStatus(String transferId, boolean status) {
            this.transferId = transferId;
            this.status = status;
        }
    }

    static public class CreditStatus {
        String transferId;
        boolean status;

        public CreditStatus(String transferId, boolean status) {
            this.transferId = transferId;
            this.status = status;
        }
    }

    static public class Terminate {
    }
}
