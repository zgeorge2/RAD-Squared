/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.apps.bank.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Props;
import com.rad2.akka.aspects.ActorMessageHandler;
import com.rad2.akka.common.BaseActorWithRegState;
import com.rad2.akka.common.RegistryStateDTO;
import com.rad2.apps.bank.akka.Account.AccrueInterest;
import com.rad2.apps.bank.akka.AccountStatement.RequestStatement;
import com.rad2.apps.bank.akka.Bank.Print;
import com.rad2.apps.bank.ignite.AccountHolderRegistry;
import com.rad2.common.serialization.IAkkaSerializable;
import com.rad2.common.utils.PrintUtils;
import com.rad2.ignite.common.RegistryManager;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AccountHolder extends BaseActorWithRegState {
    public AccountHolder(RegistryManager rm, RegistryStateDTO dto) {
        super(rm, dto);
    }

    static public Props props(RegistryManager rm, String parentKey, String name) {
        RegistryStateDTO dto = new AccountHolderRegistry.AccountHolderRegistryStateDTO(parentKey, name);
        return Props.create(AccountHolder.class, rm, dto);
    }

    private AccountHolderRegistry getAHReg() {
        return reg(AccountHolderRegistry.class);
    }

    @Override
    public Receive createReceive() {
        return super.createReceive()
                .orElse(receiveBuilder()
                        .match(SendMoney.class, this::sendMoney)
                        .match(AccrueInterest.class, this::accrueInterest)
                        .match(AccrueRewardPoints.class, this::accrueRewardPoints)
                        .match(CreateAccounts.class, this::createAccounts)
                        .match(RequestStatement.class, this::requestStatement)
                        .match(Print.class, this::print)
                        .match(SleepyNoOp.class, this::sleepyNoOp)
                        .build());
    }

    @ActorMessageHandler
    private void createAccounts(CreateAccounts acc) {
        Account.AccountNameEnum.getAllAccountNames().forEach(nm -> {
            this.add(() -> Account.props(this.getRM(), this.getRegId(), nm, acc.startingBalance), nm);
        });
    }

    @ActorMessageHandler
    private void print(Print p) {
        // create a new statement
        String stmtName = "AH_stmt_" + this.getAHReg().generateNewId();
        ActorRef accStmt = this.add(() -> AccountStatement.props(this.getRM(), p.jobRef()), stmtName);
        accStmt.tell(new AccountStatement.BeginStatementPreparation(self(), this.getAllAccounts()), self());
    }

    @ActorMessageHandler
    private void requestStatement(AccountStatement.RequestStatement x) {
        // prepare the statement and send it back to the AccountStatement (which assembles all the statement
        // parts)
        AccountHolderRegistry.DAccountHolder model = this.getAHReg().get(this.getRegId());
        sender().tell(new AccountStatement.ReceiveStatement(x.requestId, String.format("%n *** %s *** %n",
                model)), self());
    }

    @ActorMessageHandler
    private void sendMoney(SendMoney x) {
        String transId = this.getAHReg().getTransactionIdForMoneyTransfer(this.getRegId());
        // construct a MT Actor for every MT to be transacted.
        ActorRef mt = this.add(
                () -> MoneyTransfer.props(this.getRM(), transId, x.amount, x.fromAcc, x.toAcc), transId);
        mt.tell(new MoneyTransfer.ValidateAndCompleteTransfer(), self());
    }

    @ActorMessageHandler
    private void accrueInterest(Account.AccrueInterest x) {
        this.getAllAccountsList().forEach(ac -> ac.tell(x, self()));
    }

    @ActorMessageHandler
    private void accrueRewardPoints(AccrueRewardPoints x) {
        this.getAHReg().creditRewardPoints(this.getRegId(), 1); // for now hard-code 1 reward point
    }

    @ActorMessageHandler
    private void sleepyNoOp(SleepyNoOp arg) {
        try {
            Thread.sleep(arg.getSleepInMillis());
        } catch (Exception e) {
            PrintUtils.printToActor(e.toString());
        }
        PrintUtils.printToActor("****** [%s]: [%d] + [%d] = [%d]******", self().path().toString(), arg.getA(), arg.getB(), arg.getB() + arg.getA());
    }

    private List<ActorRef> getAllAccountsList() {
        return Account.AccountNameEnum.getAllAccountNames().stream()
                .map(acName -> this.context().child(acName).get())
                .collect(Collectors.toList());
    }

    private Map<String, ActorRef> getAllAccounts() {
        return Account.AccountNameEnum.getAllAccountNames().stream()
                .map(acName -> new AbstractMap.SimpleEntry<>(acName, this.context().child(acName).get()))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    /**
     * Classes used for received method above.
     */
    static public class CreateAccounts {
        public final int startingBalance;

        public CreateAccounts(int startingBalance) {
            this.startingBalance = startingBalance;
        }
    }

    static public class AccrueRewardPoints implements IAkkaSerializable {
        public AccrueRewardPoints() {
        }
    }

    static public class SendMoney {
        private int amount;
        private ActorSelection fromAcc;
        private ActorSelection toAcc;

        public SendMoney(int amount, ActorSelection fromAcc, ActorSelection toAcc) {
            this.amount = amount;
            this.fromAcc = fromAcc;
            this.toAcc = toAcc;
        }
    }

    static public class SleepyNoOp {
        private int a;
        private int b;
        private long sleepInMillis;

        public SleepyNoOp(int a, int b, long sleepInMillis) {
            this.a = a;
            this.b = b;
            this.sleepInMillis = sleepInMillis;
        }

        public long getSleepInMillis() {
            return sleepInMillis;
        }

        public int getA() {
            return a;
        }

        public void setA(int a) {
            this.a = a;
        }

        public int getB() {
            return b;
        }

        public void setB(int b) {
            this.b = b;
        }
    }
}
