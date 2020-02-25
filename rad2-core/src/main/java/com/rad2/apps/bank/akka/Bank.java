/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.apps.bank.akka;

import akka.actor.ActorRef;
import akka.actor.Props;
import com.rad2.akka.aspects.ActorMessageHandler;
import com.rad2.akka.common.BaseActorWithRegState;
import com.rad2.akka.common.BasicDeferredMessage;
import com.rad2.akka.common.IDeferred;
import com.rad2.akka.common.RegistryStateDTO;
import com.rad2.apps.bank.akka.AccountHolder.SleepyNoOp;
import com.rad2.apps.bank.ignite.AccountHolderRegistry;
import com.rad2.apps.bank.ignite.BankRegistry;
import com.rad2.ignite.common.RegistryManager;

import java.util.ArrayList;
import java.util.List;

public class Bank extends BaseActorWithRegState {
    private static final int ACCOUNT_STARTING_BALANCE = 100;

    private Bank(RegistryManager rm, RegistryStateDTO dto) {
        super(rm, dto);
    }

    static public Props props(RegistryManager rm, String parentKey, String name) {
        RegistryStateDTO dto = new BankRegistry.BankRegistryStateDTO(parentKey, name);
        return Props.create(Bank.class, rm, dto);
    }

    @Override
    public Receive createReceive() {
        return super.createReceive()
                .orElse(receiveBuilder()
                        .match(Print.class, this::print)
                        .match(CreateAccountHolder.class, this::createAccountHolder)
                        .match(GetAllAccountHolders.class, this::getAllAccountHolders)
                        .match(Account.AccrueInterest.class, this::accrueInterest)
                        .match(AccountHolder.AccrueRewardPoints.class, this::accrueRewardPoints)
                        .match(SleepyNoOp.class, this::sleepyNoOp)
                        .build());
    }

    @ActorMessageHandler
    private void print(Print p) {
        this.context().children().foreach(ahActor -> {
            ahActor.tell(p, self());
            return true;
        });
    }

    @ActorMessageHandler
    private void accrueInterest(Account.AccrueInterest i) {
        this.context().children().foreach(ahActor -> {
            ahActor.tell(i, self());
            return true;
        });
    }

    @ActorMessageHandler
    private void accrueRewardPoints(AccountHolder.AccrueRewardPoints arg) {
        this.context().children().foreach(c -> {
            c.tell(arg, self());
            return true;
        });
    }

    @ActorMessageHandler
    private void getAllAccountHolders(GetAllAccountHolders arg) {
        AccountHolderRegistry ahReg = reg(AccountHolderRegistry.class);
        GetAllAccountHoldersResult ret = new GetAllAccountHoldersResult();
        ahReg.applyToChildrenOfParent(getRegId(), ah -> ret.add(ah.getName()));
        updateJobSuccess(arg, ret.toString());
    }

    @ActorMessageHandler
    private void createAccountHolder(CreateAccountHolder cah) {
        String nm = cah.accountHolderName;
        ActorRef ah = this.add(() -> AccountHolder.props(this.getRM(), this.getRegId(), nm), nm);
        ah.tell(new AccountHolder.CreateAccounts(ACCOUNT_STARTING_BALANCE), self());
    }

    @ActorMessageHandler
    private void sleepyNoOp(SleepyNoOp arg) {
        this.context().children().foreach(c -> {
            c.tell(arg, self());
            return true;
        });
    }

    /**
     * Classes used for received method above.
     */
    static public class CreateAccountHolder {
        public String accountHolderName;

        public CreateAccountHolder(String accountHolderName) {
            this.accountHolderName = accountHolderName;
        }
    }

    static public class BankRequest extends BasicDeferredMessage<String> {
        public static final String BANK_NAME_KEY = "BANK_NAME_KEY";

        public BankRequest(IDeferred<String> req) {
            super(req);
        }

        public String bankName() {
            return (String) arg(BANK_NAME_KEY);
        }
    }

    static public class Print extends BankRequest {
        public Print(IDeferred<String> req) {
            super(req);
        }

        public Print() {
            super(null);
        }
    }

    static public class GetAllAccountHolders extends BankRequest {
        public GetAllAccountHolders(IDeferred<String> req) {
            super(req);
        }

    }

    static public class GetAllAccountHoldersResult {
        List<String> accountHolderNames;

        public GetAllAccountHoldersResult() {
            this.accountHolderNames = new ArrayList<>();
        }

        public GetAllAccountHoldersResult add(String ahName) {
            this.accountHolderNames.add(ahName);
            return this;
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            this.accountHolderNames.forEach(ahName -> {
                sb.append(String.format("******* AH = [%s] *******", ahName));
            });
            return sb.toString();
        }
    }
}

