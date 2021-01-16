/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.apps.bank.akka;

import akka.actor.ActorSelection;
import akka.actor.Props;
import com.rad2.akka.aspects.ActorMessageHandler;
import com.rad2.akka.common.BaseActor;
import com.rad2.common.utils.PrintUtils;
import com.rad2.ignite.common.RegistryManager;

/**
 * A MoneyTransfer Actor is always created to handle each transaction. This decouples the handling logic from
 * the accounts. This Actor then coordinates the transfer from one account to another. If need be, TPC
 * protocols can be introduced here.
 */
public class MoneyTransfer extends BaseActor {
    private String tranId;
    private int amount;
    private ActorSelection fromAcc;
    private ActorSelection toAcc;

    public MoneyTransfer(RegistryManager rm, String tranId, int amount,
                         ActorSelection fromAcc, ActorSelection toAcc) {
        super(rm);
        this.tranId = tranId;
        this.amount = amount;
        this.fromAcc = fromAcc;
        this.toAcc = toAcc;
    }

    static public Props props(RegistryManager rm, String tranId, int amount,
                              ActorSelection fromAcc, ActorSelection toAcc) {
        return Props.create(MoneyTransfer.class, rm, tranId, amount, fromAcc, toAcc);
    }

    @Override
    public String toString() {
        return String.format("MT[%s][Rs. %d] FROM [%s] TO [%s]",
            getTransactionId(), getAmount(), getFromAcc(), getToAcc());
    }

    @Override
    public Receive createReceive() {
        return super.createReceive()
            .orElse(receiveBuilder()
                .match(ValidateAndCompleteTransfer.class, this::validateAndCompleteTransfer)
                .match(Terminate.class, this::terminate)
                .build());
    }

    @ActorMessageHandler
    private void validateAndCompleteTransfer(ValidateAndCompleteTransfer arg) {
        if (getFromAcc() == null || getToAcc() == null) {
            PrintUtils.print("Cannot validate and complete transfer. Account setup is pending!");
            return; // the from/to accounts need to be non-null
        }
        // debit the fromAccount
        String dDetails = String.format("DEBITED FOR: %s", this.toString());
        getFromAcc().tell(new Account.Debit(this.getAmount(), dDetails), self());
        // credit the toAccount
        String cDetails = String.format("CREDITED TO: %s", this.toString());
        getToAcc().tell(new Account.Credit(this.getAmount(), cDetails), self());
        // this is the last use of this Actor - terminate it.
        self().tell(new Terminate(), self());
    }

    @ActorMessageHandler
    private void terminate(Terminate arg) {
        this.context().stop(self());
    }

    private String getTransactionId() {
        return tranId;
    }

    private int getAmount() {
        return amount;
    }

    private ActorSelection getFromAcc() {
        return fromAcc;
    }

    private ActorSelection getToAcc() {
        return toAcc;
    }

    static public class ValidateAndCompleteTransfer {
    }

    static public class Terminate {
    }
}
