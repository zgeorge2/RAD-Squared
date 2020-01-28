package com.rad2.apps.bank.ignite;

import com.rad2.akka.common.RegistryStateDTO;
import com.rad2.ignite.common.BaseModelRegistry;
import com.rad2.ignite.common.DModel;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AccountTransactionRegistry
    extends BaseModelRegistry<AccountTransactionRegistry.DAccountTransaction> {
    @Override
    protected Class getModelClass() {
        return DAccountTransaction.class;
    }

    @Override
    public Class getParentRegistryClass() {
        return AccountRegistry.class;
    }

    @Override
    protected String getDefaultOrderByColumn() {
        return "date";
    }

    public static class DAccountTransaction extends DModel {
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

        public DAccountTransaction(AccountRegistry.EntryDTO entryDTO) {
            super(entryDTO);
            this.amount = entryDTO.getAmount();
            this.balance = entryDTO.getBalance();
            this.date = System.currentTimeMillis();
            this.type = entryDTO.getType();
            this.details = entryDTO.getDetails();
        }

        @Override
        public RegistryStateDTO toRegistryStateDTO() {
            return new AccountRegistry.EntryDTO(this);
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
}

