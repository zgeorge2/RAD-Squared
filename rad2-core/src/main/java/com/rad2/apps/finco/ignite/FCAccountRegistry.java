/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.apps.finco.ignite;

import com.rad2.akka.common.RegistryStateDTO;
import com.rad2.apps.bank.akka.Account;
import com.rad2.apps.bank.ignite.AccountHolderRegistry;
import com.rad2.apps.finco.akka.FCAccountTypes;
import com.rad2.ignite.common.BaseModelRegistry;
import com.rad2.ignite.common.DModel;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

public class FCAccountRegistry extends BaseModelRegistry<FCAccountRegistry.D_FC_Account> {
    @Override
    public void postAdd(RegistryStateDTO dto) {
        FCAccountRegistryStateDTO acDTO = (FCAccountRegistryStateDTO) dto;
        // Enter this starting account balance into AccountEntryRegistry
        this.initAccount(acDTO.getKey(), acDTO.getBalance());
    }

    private FCAccountEntryRegistry getAEReg() {
        return this.reg(FCAccountEntryRegistry.class);
    }

    @Override
    public Class getModelClass() {
        return D_FC_Account.class;
    }

    @Override
    public Class getParentRegistryClass() {
        return AccountHolderRegistry.class;
    }

    public String createAccount(String parentKey, String type, int balance) {
        // create an account Number
        String accNum = "ACC_" + this.generateNewId();
        return this.add(new FCAccountRegistryStateDTO(parentKey, type, accNum, balance));
    }

    private String initAccount(String key, int balance) {
        String tranId = "" + this.getAEReg().generateNewId();
        return this.getAEReg().add(this.apply(key, cc -> cc.init(tranId, balance)));
    }

    public String debitAccount(String key, int amount, String details) {
        String tranId = "" + this.getAEReg().generateNewId();
        return this.getAEReg().add(this.apply(key, cc -> cc.debit(tranId, amount, details)));
    }

    public String creditAccount(String key, int amount, String details) {
        String tranId = "" + this.getAEReg().generateNewId();
        return this.getAEReg().add(this.apply(key, cc -> cc.credit(tranId, amount, details)));
    }

    public void accrueInterest(String finCo) {
        String tranId = "INT_CREDIT_" + this.generateNewId();
        this.getAll()
                .stream()
                .filter(acc -> acc.getKey().startsWith(finCo + "/"))
                .forEach(acc -> {
                    this.getAEReg().add(this.apply(acc.getKey(), cc -> cc.accrueInterest(tranId)));
                });
    }

    public String prepareBalanceReport(String key) {
        StringBuffer sb = new StringBuffer();
        // print the account details
        this.apply(key, ac -> sb.append(ac.toString()));
        // print the header for the transactions table
        sb.append(FCAccountEntryRegistry.D_FC_AccountEntry.HEADER_STRING);
        // print each account transaction entry
        this.getAEReg().applyToChildrenOfParent(key, e -> sb.append(e.toString()));

        return sb.toString();
    }

    public static class D_FC_Account extends DModel {
        private static final String CREDIT = "C";
        private static final String DEBIT = "D";
        @QuerySqlField
        private final String accountNum;
        @QuerySqlField
        private int balance;
        @QuerySqlField
        private long lastTransactionId;

        public D_FC_Account(FCAccountRegistryStateDTO dto) {
            super(dto);
            this.balance = dto.getBalance();
            this.accountNum = dto.getAccountNumber();
            this.lastTransactionId = dto.getLastTransactionId();
        }

        @Override
        public RegistryStateDTO toRegistryStateDTO() {
            return new FCAccountRegistryStateDTO(this);
        }

        @Override
        public Class getActorClass() {
            return Account.class;
        }

        public String getType() {
            return this.getName();
        }

        public String getAccountNumber() {
            return this.accountNum;
        }

        public int getBalance() {
            return this.balance;
        }

        public long getLastTransactionId() {
            return lastTransactionId;
        }

        public int getPercent() {
            return FCAccountTypes.valueOf(getType()).getPercent();
        }

        public FCAccountEntryRegistry.FCAccountEntryDTO init(String tranId, int balance) {
            this.balance = balance;
            return new FCAccountEntryRegistry.FCAccountEntryDTO(this.getKey(), tranId, balance, this.balance, CREDIT, "Starting Balance");
        }

        public FCAccountEntryRegistry.FCAccountEntryDTO debit(String tranId, int amount, String details) {
            this.balance -= amount;
            return new FCAccountEntryRegistry.FCAccountEntryDTO(this.getKey(), tranId, amount, this.balance, DEBIT, details);
        }

        public FCAccountEntryRegistry.FCAccountEntryDTO credit(String tranId, int amount, String details) {
            this.balance += amount;
            return new FCAccountEntryRegistry.FCAccountEntryDTO(this.getKey(), tranId, amount, this.balance, CREDIT, details);
        }

        public FCAccountEntryRegistry.FCAccountEntryDTO accrueInterest(String tranId) {
            int interest = (this.getPercent() * this.balance) / 100;
            String details = String.format("INT CREDITED @%s%%", this.getPercent());
            this.balance += interest;
            return new FCAccountEntryRegistry.FCAccountEntryDTO(this.getKey(), tranId, interest, this.balance, CREDIT, details);
        }

        synchronized String getNextTransactionId() {
            this.lastTransactionId++;
            return String.format("MT_TID_%d", this.lastTransactionId);
        }
    }

    public static class FCAccountRegistryStateDTO extends RegistryStateDTO {
        public static final String ATTR_BALANCE_KEY = "BALANCE_KEY";
        public static final String ATTR_ACCOUNT_NUM_KEY = "ACCOUNT_NUM_KEY";
        public static final String ATTR_LAST_TRANSACTION_ID_KEY = "LAST_TRANSACTION_ID_KEY";

        public FCAccountRegistryStateDTO(String parentKey, String name,
                                         String accNum, int balance) {
            super(FCAccountRegistry.class, parentKey, name);
            this.putAttr(ATTR_BALANCE_KEY, balance);
            this.putAttr(ATTR_ACCOUNT_NUM_KEY, accNum);
            this.putAttr(ATTR_LAST_TRANSACTION_ID_KEY, 0L); // initialized to 0
        }

        public FCAccountRegistryStateDTO(D_FC_Account model) {
            super(FCAccountRegistry.class, model);
            this.putAttr(ATTR_BALANCE_KEY, model.getBalance());
            this.putAttr(ATTR_ACCOUNT_NUM_KEY, model.getType());
            this.putAttr(ATTR_LAST_TRANSACTION_ID_KEY, model.getLastTransactionId());
        }

        @Override
        public DModel toModel() {
            return new D_FC_Account(this);
        }

        public String getType() {
            return this.getName();
        }

        public String getAccountNumber() { // the name is the account id
            return (String) this.getAttr(ATTR_ACCOUNT_NUM_KEY);
        }

        public int getBalance() {
            return (int) this.getAttr(ATTR_BALANCE_KEY);
        }

        public long getLastTransactionId() {
            return (long) this.getAttr(ATTR_LAST_TRANSACTION_ID_KEY);
        }

        public int getPercent() {
            return FCAccountTypes.valueOf(getType()).getPercent();
        }
    }

}
