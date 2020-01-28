package com.rad2.apps.bank.ignite;

import com.rad2.akka.common.RegistryStateDTO;
import com.rad2.apps.bank.akka.Account;
import com.rad2.ignite.common.BaseModelRegistry;
import com.rad2.ignite.common.DModel;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

public class AccountRegistry extends BaseModelRegistry<AccountRegistry.DAccount> {
    @Override
    public void postAdd(RegistryStateDTO dto) {
        AccountRegistryStateDTO acDTO = (AccountRegistryStateDTO) dto;
        // Enter this starting account balance into AccountTranReg
        this.initAccount(acDTO.getKey(), acDTO.getBalance());
    }

    private AccountTransactionRegistry getATReg() {
        return this.reg(AccountTransactionRegistry.class);
    }

    @Override
    public Class getModelClass() {
        return DAccount.class;
    }

    @Override
    public Class getParentRegistryClass() {
        return AccountHolderRegistry.class;
    }

    private String initAccount(String key, int balance) {
        String tranId = "" + this.getATReg().generateNewId();
        return this.getATReg().add(this.apply(key, cc -> cc.init(tranId, balance)));
    }

    public String debitAccount(String key, int amount, String details) {
        String tranId = "" + this.getATReg().generateNewId();
        return this.getATReg().add(this.apply(key, cc -> cc.debit(tranId, amount, details)));
    }

    public String creditAccount(String key, int amount, String details) {
        String tranId = "" + this.getATReg().generateNewId();
        return this.getATReg().add(this.apply(key, cc -> cc.credit(tranId, amount, details)));
    }

    public String accrueInterest(String key, int percent) {
        String tranId = "" + this.getATReg().generateNewId();
        return this.getATReg().add(this.apply(key, cc -> cc.accrueInterest(tranId, percent)));
    }

    public String prepareBalanceReport(String key) {
        StringBuffer sb = new StringBuffer();
        // print the account details
        this.apply(key, ac -> sb.append(ac.toString()));
        // print the header for the transactions table
        sb.append(AccountTransactionRegistry.DAccountTransaction.HEADER_STRING);
        // print each account transaction entry
        this.getATReg().applyToChildrenOfParent(key, e -> sb.append(e.toString()));

        return sb.toString();
    }

    public static class DAccount extends DModel {
        private static final String CREDIT = "C";
        private static final String DEBIT = "D";
        @QuerySqlField
        private int balance;

        public DAccount(AccountRegistryStateDTO dto) {
            super(dto);
            this.balance = 0;
        }

        @Override
        public RegistryStateDTO toRegistryStateDTO() {
            return new AccountRegistryStateDTO(this);
        }

        @Override
        public Class getActorClass() {
            return Account.class;
        }

        public int getBalance() {
            return this.balance;
        }

        public EntryDTO init(String tranId, int balance) {
            this.balance = balance;
            return new EntryDTO(this.getKey(), tranId, balance, this.balance, CREDIT, "Starting Balance");
        }

        public EntryDTO debit(String tranId, int amount, String details) {
            this.balance -= amount;
            return new EntryDTO(this.getKey(), tranId, amount, this.balance, DEBIT, details);
        }

        public EntryDTO credit(String tranId, int amount, String details) {
            this.balance += amount;
            return new EntryDTO(this.getKey(), tranId, amount, this.balance, CREDIT, details);
        }

        public EntryDTO accrueInterest(String tranId, int percent) {
            int interest = (percent * this.balance) / 100;
            String details = String.format("Interest payment@%s%%", percent);
            this.balance += interest;
            return new EntryDTO(this.getKey(), tranId, interest, this.balance, CREDIT, details);
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(super.toString());
            sb.append(String.format("\t[BALANCE:%s]\n", this.getBalance()));
            return sb.toString();
        }
    }

    public static class AccountRegistryStateDTO extends RegistryStateDTO {
        public static final String ATTR_STARTING_BALANCE_KEY = "STARTING_BALANCE_KEY";

        public AccountRegistryStateDTO(String parentKey, String name, int balance) {
            super(AccountRegistry.class, parentKey, name);
            this.putAttr(ATTR_STARTING_BALANCE_KEY, balance);
        }

        public AccountRegistryStateDTO(DAccount model) {
            super(AccountRegistry.class, model);
            this.putAttr(ATTR_STARTING_BALANCE_KEY, model.getBalance());
        }

        @Override
        public DModel toModel() {
            return new DAccount(this);
        }

        public int getBalance() {
            return (int) this.getAttr(ATTR_STARTING_BALANCE_KEY);
        }
    }

    public static class EntryDTO extends RegistryStateDTO {
        // the parent account of this Entry
        private static final String ATTR_AMOUNT_KEY = "AMOUNT_KEY";
        private static final String ATTR_BALANCE_KEY = "BALANCE_KEY";
        private static final String ATTR_TYPE_KEY = "TYPE_KEY"; // credit or debit
        private static final String ATTR_DETAILS_KEY = "DETAILS_KEY";// money transfer transaction name

        public EntryDTO(String parentKey, String name, int amount, int balance, String type, String details) {
            super(AccountTransactionRegistry.class, parentKey, name);
            this.putAttr(ATTR_AMOUNT_KEY, amount);
            this.putAttr(ATTR_BALANCE_KEY, balance);
            this.putAttr(ATTR_TYPE_KEY, type);
            this.putAttr(ATTR_DETAILS_KEY, details);
        }

        public EntryDTO(AccountTransactionRegistry.DAccountTransaction model) {
            super(AccountTransactionRegistry.class, model);
            this.putAttr(ATTR_AMOUNT_KEY, model.getAmount());
            this.putAttr(ATTR_BALANCE_KEY, model.getBalance());
            this.putAttr(ATTR_TYPE_KEY, model.getType());
            this.putAttr(ATTR_DETAILS_KEY, model.getDetails());
        }

        @Override
        public DModel toModel() {
            return new AccountTransactionRegistry.DAccountTransaction(this);
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
