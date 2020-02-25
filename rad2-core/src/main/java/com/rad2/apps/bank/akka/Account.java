/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.apps.bank.akka;

import akka.actor.Props;
import com.rad2.akka.aspects.ActorMessageHandler;
import com.rad2.akka.common.BaseActorWithRegState;
import com.rad2.akka.common.RegistryStateDTO;
import com.rad2.apps.bank.ignite.AccountRegistry;
import com.rad2.common.serialization.IAkkaSerializable;
import com.rad2.ignite.common.RegistryManager;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Account extends BaseActorWithRegState {
    public Account(RegistryManager rm, RegistryStateDTO dto) {
        super(rm, dto);
    }

    static public Props props(RegistryManager rm, String parentKey, String name, int startingBalance) {
        RegistryStateDTO dto = new AccountRegistry.AccountRegistryStateDTO(parentKey, name, startingBalance);
        return Props.create(Account.class, rm, dto);
    }

    private AccountRegistry getACReg() {
        return reg(AccountRegistry.class);
    }

    @Override
    public Receive createReceive() {
        return super.createReceive()
            .orElse(receiveBuilder()
                .match(Credit.class, this::creditAccount)
                .match(Debit.class, this::debitAccount)
                .match(AccrueInterest.class, this::accrueInterest)
                .match(AccountStatement.RequestStatement.class, this::requestStatement)
                .build());
    }

    @ActorMessageHandler
    private String debitAccount(Debit d) {
        return this.getACReg().debitAccount(this.getRegId(), d.removeFromAccount, d.details);
    }

    @ActorMessageHandler
    private String creditAccount(Credit c) {
        return this.getACReg().creditAccount(this.getRegId(), c.addToAccount, c.details);
    }

    @ActorMessageHandler
    private String accrueInterest(AccrueInterest c) {
        return this.getACReg().accrueInterest(this.getRegId(), c.percent);
    }

    @ActorMessageHandler
    private void requestStatement(AccountStatement.RequestStatement x) {
        sender().tell(new AccountStatement.ReceiveStatement(x.requestId, this.balanceReport()), self());
    }

    private String balanceReport() {
        return this.getACReg().prepareBalanceReport(this.getRegId());
    }

    public enum AccountNameEnum {
        NRO("NRO"),
        NRE("NRE");
        private String name;

        AccountNameEnum(String name) {
            this.name = name;
        }

        public static List<String> getAllAccountNames() {
            return Arrays.stream(values()).map(AccountNameEnum::getName).collect(Collectors.toList());
        }

        public static String validateAccountName(String accountName) {
            return getAllAccountNames()
                .stream()
                .filter(accountName::equalsIgnoreCase)
                .findAny()
                .orElse(null);
        }

        public static void applyToAccountNames(Consumer<String> func) {
            getAllAccountNames().forEach(n -> {
                func.accept(n);
            });
        }

        public String getName() {
            return name;
        }

        public String toString() {
            return this.name;
        }
    }

    /**
     * Each class below represents a statement that can be received by this Actor. Note that these messages
     * are immutable structures. Message handling is done in the "createReceive" method of the Account Actor
     * class.
     */
    static public class AccrueInterest {
        private static final int DEFAULT_INTEREST_RATE = 8;
        int percent;

        public AccrueInterest() {
            this(DEFAULT_INTEREST_RATE);
        }

        public AccrueInterest(int percent) {
            this.percent = percent;
        }
    }

    static public class Credit implements IAkkaSerializable {
        public final int addToAccount;
        public final String details;

        public Credit() {
            this(0, "");
        }

        public Credit(int addToAccount, String details) {
            this.addToAccount = addToAccount;
            this.details = details;
        }
    }

    static public class Debit implements IAkkaSerializable {
        public final int removeFromAccount;
        public final String details;

        public Debit() {
            this(0, null);
        }

        public Debit(int removeFromAccount, String details) {
            this.removeFromAccount = removeFromAccount;
            this.details = details;
        }
    }
}

