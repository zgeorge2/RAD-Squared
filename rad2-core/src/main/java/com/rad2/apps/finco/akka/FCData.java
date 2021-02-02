package com.rad2.apps.finco.akka;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * This contains the DTO classes used by the FinCo app
 */
public class FCData {
    private static final ObjectMapper om = new ObjectMapper();

    @Override
    public String toString() {
        String ret = "";
        try {
            ret = om.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return ret;

    }

    public static class FinCoList extends FCData {
        private final List<FinCo> finCoList;

        public FinCoList() {
            this.finCoList = new ArrayList<>();
        }

        @JsonProperty
        public List<FinCo> getFinCoList() {
            return finCoList;
        }
    }

    public static class FinCoByIdList extends FCData {
        private final List<FinCoById> finCoByIdList;

        public FinCoByIdList() {
            this.finCoByIdList = new ArrayList<>();
        }

        @JsonProperty
        public List<FinCoById> getFinCoByIdList() {
            return finCoByIdList;
        }
    }

    public static class FinCoBase extends FCData {
        private final List<FCAccHol> accountHolders;

        public FinCoBase() {
            this.accountHolders = new ArrayList<>();
        }

        @JsonProperty
        public List<FCAccHol> getAccountHolders() {
            return accountHolders;
        }
    }

    public static class FinCo extends FinCoBase {
        private String name; // the name of the Fin Co (e.g., Citibank)
        private String branch; // the LAX branch

        public FinCo() {
            super();
        }

        @JsonProperty
        public String getName() {
            return name;
        }

        @JsonProperty
        public String getBranch() {
            return branch;
        }
    }

    public static class FinCoById extends FinCoBase {
        private String finCoId; // the FC Branch id (e.g.,Citibank/Bank1)

        public FinCoById() {
            super();
        }

        @JsonProperty
        public String getFinCoId() {
            return finCoId;
        }
    }

    public static class FCAccHol extends FCData {
        private String name; // the name of the Account Holder (e.g., Bob)
        private final List<FCAcc> accounts; // list of accounts opened

        public FCAccHol() {
            this.accounts = new ArrayList<>();
        }

        @JsonProperty
        public String getName() {
            return name;
        }

        @JsonProperty
        public List<FCAcc> getAccounts() {
            return accounts;
        }
    }

    public static class FCAcc extends FCData {
        private String type; // e.g., See FCAccountTypes
        private int balance; // opening balance

        public FCAcc() {
        }

        @JsonProperty
        public String getType() {
            return type;
        }

        @JsonProperty
        public int getBalance() {
            return balance;
        }
    }

    public static class FCTransfers extends FCData {
        private final List<FCTransfer> transfers;

        public FCTransfers() {
            this.transfers = new ArrayList<>();
        }

        @JsonProperty
        public List<FCTransfer> getTransfers() {
            return transfers;
        }
    }

    public static class FCTransfer extends FCData {
        private String from; // account from which money will be debited
        private String to; // account to which money will be credited
        private int amount; // amount to transfer

        @JsonProperty
        public String getFrom() {
            return from;
        }

        @JsonProperty
        public String getTo() {
            return to;
        }

        @JsonProperty
        public int getAmount() {
            return amount;
        }
    }
}
