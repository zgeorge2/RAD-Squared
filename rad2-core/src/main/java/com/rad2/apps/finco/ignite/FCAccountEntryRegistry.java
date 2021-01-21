/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.apps.finco.ignite;

import com.rad2.akka.common.RegistryStateDTO;
import com.rad2.ignite.common.BaseModelRegistry;
import com.rad2.ignite.common.DModel;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FCAccountEntryRegistry
        extends BaseModelRegistry<FCAccountEntryRegistry.D_FC_AccountEntry> {
    @Override
    protected Class getModelClass() {
        return D_FC_AccountEntry.class;
    }

    @Override
    public Class getParentRegistryClass() {
        return FCAccountRegistry.class;
    }

    @Override
    protected String getDefaultOrderByColumn() {
        return "date";
    }

    public static class D_FC_AccountEntry extends DModel {
        private static final String HEADER_FORMAT = "|%30.25s|%30.30s|%8s|%30.30s|%10s|%10s|%n";
        public static final String HEADER_STRING =
                String.format(HEADER_FORMAT, "key", "Date", "Type", "Details", "Amount", "Balance");
        private static final String ENTRY_FORMAT = "|%30.25s|%30.30s|%8s|%30.30s|%10d|%10d|%n";
        private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM HH:mm:ss");
        @QuerySqlField
        private final int amount;
        @QuerySqlField
        private final int balance;
        @QuerySqlField
        private final long date;
        @QuerySqlField
        private final String type; // credit or debit
        @QuerySqlField
        private final String details; // e.g. money transfer transaction name

        public D_FC_AccountEntry(FCAccountEntryDTO accountEntryDTO) {
            super(accountEntryDTO);
            this.amount = accountEntryDTO.getAmount();
            this.balance = accountEntryDTO.getBalance();
            this.date = System.currentTimeMillis();
            this.type = accountEntryDTO.getType();
            this.details = accountEntryDTO.getDetails();
        }

        @Override
        public RegistryStateDTO toRegistryStateDTO() {
            return new FCAccountEntryDTO(this);
        }

        public int getAmount() {
            return amount;
        }

        public int getBalance() {
            return balance;
        }

        public long getDate() {
            return date;
        }

        public String getType() {
            return type;
        }

        public String getDetails() {
            return details;
        }

        public String toString() {
            String dateString = DATE_FORMAT.format(new Date(this.date));
            return String.format(ENTRY_FORMAT, getKey(), dateString, getType(), getDetails(),
                    getAmount(), getBalance());
        }
    }

    public static class FCAccountEntryDTO extends RegistryStateDTO {
        // the parent account of this Entry
        private static final String ATTR_AMOUNT_KEY = "AMOUNT_KEY";
        private static final String ATTR_BALANCE_KEY = "BALANCE_KEY";
        private static final String ATTR_TYPE_KEY = "TYPE_KEY"; // credit or debit
        private static final String ATTR_DETAILS_KEY = "DETAILS_KEY";// money transfer transaction name

        public FCAccountEntryDTO(String parentKey, String name, int amount, int balance, String type, String details) {
            super(FCAccountEntryRegistry.class, parentKey, name);
            this.putAttr(ATTR_AMOUNT_KEY, amount);
            this.putAttr(ATTR_BALANCE_KEY, balance);
            this.putAttr(ATTR_TYPE_KEY, type);
            this.putAttr(ATTR_DETAILS_KEY, details);
        }

        public FCAccountEntryDTO(D_FC_AccountEntry model) {
            super(FCAccountEntryRegistry.class, model);
            this.putAttr(ATTR_AMOUNT_KEY, model.getAmount());
            this.putAttr(ATTR_BALANCE_KEY, model.getBalance());
            this.putAttr(ATTR_TYPE_KEY, model.getType());
            this.putAttr(ATTR_DETAILS_KEY, model.getDetails());
        }

        @Override
        public DModel toModel() {
            return new D_FC_AccountEntry(this);
        }

        public int getAmount() {
            return (int) this.getAttr(ATTR_AMOUNT_KEY);
        }

        public int getBalance() {
            return (int) this.getAttr(ATTR_BALANCE_KEY);
        }

        public String getType() {
            return (String) this.getAttr(ATTR_TYPE_KEY);
        }

        public String getDetails() {
            return (String) this.getAttr(ATTR_DETAILS_KEY);
        }

        public String toString() {
            return String.format("[%s/%s]:[%d][%d][%s][%s]", this.getParentKey(), this.getName(),
                this.getAmount(), this.getBalance(), this.getType(), this.getDetails());
        }
    }
}

