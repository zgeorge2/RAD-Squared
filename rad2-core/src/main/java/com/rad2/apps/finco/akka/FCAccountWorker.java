/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.apps.finco.akka;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.dispatch.BoundedMessageQueueSemantics;
import akka.dispatch.RequiresMessageQueue;
import com.rad2.akka.aspects.ActorMessageHandler;
import com.rad2.akka.common.BaseActor;
import com.rad2.akka.common.BasicDeferredMessage;
import com.rad2.akka.common.IDeferred;
import com.rad2.akka.router.WorkerActor;
import com.rad2.akka.router.WorkerClassArgs;
import com.rad2.apps.finco.ignite.FCAccountRegistry;
import com.rad2.ignite.common.RegistryManager;

import java.util.List;

public class FCAccountWorker extends BaseActor implements WorkerActor,
        RequiresMessageQueue<BoundedMessageQueueSemantics> {
    public static final String FC_ACCOUNT_MASTER_ROUTER = "FC_ACCOUNT_Master";
    public static final String BANNER_KEY = "FC_ACCOUNT_BANNER_KEY";
    private String id; // the id of this routee.
    private String banner; // an arbitrary placeholder arg - used for nothing

    private FCAccountWorker(RegistryManager rm, String id, String banner) {
        super(rm);
        this.id = id;
        this.banner = banner;
    }

    static public Props props(WorkerClassArgs args) {
        return Props.create(FCAccountWorker.class, args.getRM(), args.getId(),
                args.getArg(BANNER_KEY));
    }

    private FCAccountRegistry getACCReg() {
        return reg(FCAccountRegistry.class);
    }

    @Override
    public Receive createReceive() {
        return super.createReceive()
                .orElse(receiveBuilder()
                        .match(DoTransfers.class, this::doTransfers)
                        .match(Credit.class, this::credit)
                        .match(Debit.class, this::debit)
                        .match(AccrueInterest.class, this::accrueInterest)
                        .build());
    }

    @ActorMessageHandler
    private void doTransfers(DoTransfers arg) {
        String mtId = "MT_" + getACCReg().generateNewId();
        ActorRef mt = this.add(() -> FCMoneyTransfer.props(this.getRM(), arg.jobRef(), arg.getTransfers()), mtId);
        mt.tell(new FCMoneyTransfer.BeginTransfers(), self());
    }

    @ActorMessageHandler
    private void debit(Debit d) {
        this.getACCReg().debitAccount(d.tr.getFrom(), d.tr.getAmount(), d.details);
        sender().tell(new FCMoneyTransfer.DebitStatus(d.transferId, true), self());
    }

    @ActorMessageHandler
    private void credit(Credit c) {
        this.getACCReg().creditAccount(c.tr.getTo(), c.tr.getAmount(), c.details);
        sender().tell(new FCMoneyTransfer.CreditStatus(c.transferId, true), self());
    }

    @ActorMessageHandler
    private void accrueInterest(AccrueInterest c) {
        this.getACCReg().accrueInterest(c.finCo());
        updateJobSuccess(c, "INT CREDITED to ALL ACCOUNTS");
    }

    /**
     * Classes used for receive method above.
     */
    static public class DoTransfers extends BasicDeferredMessage<String> {
        public static final String FC_TRANSFERS_KEY = "FC_TRANSFERS_KEY";

        public DoTransfers(IDeferred<String> req) {
            super(req);
        }

        public List<FCData.FCTransfer> getTransfers() {
            return ((FCData.FCTransfers) arg(FC_TRANSFERS_KEY)).getTransfers();
        }
    }

    static public class MultiAccountOp {
        final String transferId;
        final FCData.FCTransfer tr;
        final String details;

        public MultiAccountOp(String transferId, FCData.FCTransfer tr, String details) {
            this.transferId = transferId;
            this.tr = tr;
            this.details = details;
        }
    }

    static public class Credit extends MultiAccountOp {
        public Credit(String transferId, FCData.FCTransfer t, String details) {
            super(transferId, t, details);
        }
    }

    static public class Debit extends MultiAccountOp {
        public Debit(String transferId, FCData.FCTransfer t, String details) {
            super(transferId, t, details);
        }
    }

    static public class AccountOp extends BasicDeferredMessage<String> {
        public static final String FINCO_NAME_KEY = "FINCO_NAME_KEY";

        public AccountOp(IDeferred<String> req) {
            super(req);
        }

        public String finCo() {
            return (String) arg(FINCO_NAME_KEY);
        }
    }

    static public class AccrueInterest extends AccountOp {
        public AccrueInterest(IDeferred<String> req) {
            super(req);
        }
    }
}

