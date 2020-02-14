package com.rad2.apps.bank.ctrl;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rad2.akka.common.AkkaAskAndWait;
import com.rad2.akka.common.IDeferredRequest;
import com.rad2.apps.bank.akka.Account;
import com.rad2.apps.bank.akka.AccountHolder;
import com.rad2.apps.bank.akka.Bank;
import com.rad2.apps.bank.akka.BankingCentral;
import com.rad2.ctrl.BaseController;
import com.rad2.ctrl.deps.IFakeControllerDependency;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * The BankController acts as the central brain of the application, it receives instructions from the
 * BankResource and uses the AkkaActorSystemUtility and the Apache Ignite Registry to perform those
 * instructions. The Akka Actor System performs the asynchronous, distributed, concurrent programming. All
 * shared state across Actors are held within the Ignite registry and is available to all Actors on any Node
 * in the cluster.
 */
public class BankController extends BaseController {
    public String greeting(String pathvar, String formvar, BanksDTO banks) {
        this.getFakeControllerDependency().doSomething();
        this.getFakeControllerDependency().doSomethingElse();
        return String.format("Hello PathVar=[%s] and FormVar=[%s]:[%s]", pathvar, formvar, banks);
    }

    public void initiateFibonacci(int senderRewards, int numLoops) {
        this.getBankingCentral().tell(new BankingCentral.InitiateFibonacci(senderRewards, numLoops),
                ActorRef.noSender());
    }

    public void addLocalBankAndAccountHolders(BanksDTO banks) {
        String systemName = this.getAU().getLocalSystemName();
        banks.getBanks().forEach(b -> {
            getAU().add(() -> Bank.props(getRM(), systemName, b.getName()), b.getName());
            b.getAccountHolders().forEach(accountHolder -> {
                this.addAccountHolder(b.getName(), accountHolder);
            });
        });
    }

    public void addAccountHolder(String bankName, String accountHolderName) {
        ActorSelection bank = getBank(bankName);
        bank.tell(new Bank.CreateAccountHolder(accountHolderName), ActorRef.noSender());
    }

    public void intraAccountHolderMoneyTransfer(String fromBankName, String fromAHName, String fromAccount,
                                                String toAccount, int amount) {
        String fromSysName = this.getAU().getLocalSystemName();
        String fromAccName = Account.AccountNameEnum.validateAccountName(fromAccount);
        String toSysName = fromSysName; // intra account - so same system
        String toBankName = fromBankName; // intra account - so same bank
        String toAHName = fromAHName; // intra account - so same account holder
        String toAccName = Account.AccountNameEnum.validateAccountName(toAccount);
        transferMoney(fromSysName, fromBankName, fromAHName, fromAccName, toSysName, toBankName,
                toAHName, toAccName, amount);
    }

    public void interAccountHolderMoneyTransfer(String fromBankName, String fromAHName,
                                                String toSysName, String toBankName, String toAHName,
                                                int amount) {
        String fromSysName = this.getAU().getLocalSystemName();
        String fromAccName = Account.AccountNameEnum.NRO.getName();
        String toAccName = fromAccName; // use the same type of account
        transferMoney(fromSysName, fromBankName, fromAHName, fromAccName, toSysName, toBankName,
                toAHName, toAccName, amount);
    }

    private void transferMoney(String fromSysName, String fromBankName, String fromAHName, String fromAccName,
                               String toSysName, String toBankName, String toAHName, String toAccName,
                               int amount) {
        ActorSelection fromAcc = getAU().getActor(fromSysName, fromBankName, fromAHName, fromAccName);
        ActorSelection toAcc = getAU().getActor(toSysName, toBankName, toAHName, toAccName);

        ActorSelection fromAccountHolder = getAU().getActor(fromSysName, fromBankName, fromAHName);
        fromAccountHolder.tell(new AccountHolder.SendMoney(amount, fromAcc, toAcc), ActorRef.noSender());
    }

    public void accrueMonthlyBankInterestInLocalBanks() {
        this.getBankingCentral().tell(new BankingCentral.AccrueMonthlyBankInterestInLocalBanks(),
                ActorRef.noSender());
    }

    public String getAllAccountHoldersFromBank(String bankName) {
        ActorSelection bank = this.getBank(bankName);
        AkkaAskAndWait<Bank.GetAllAccountHolders, Bank.GetAllAccountHoldersResult> ask =
                new AkkaAskAndWait<>(bank);
        Bank.GetAllAccountHoldersResult ret = ask.askAndWait(new Bank.GetAllAccountHolders(), 10);
        return ret.toString();
    }

    public String getAllAccountHolders() {
        AkkaAskAndWait<BankingCentral.GetAllAccountHolders,
                BankingCentral.AllAccHoldersResult> ask =
                new AkkaAskAndWait<>(this.getBankingCentral());
        BankingCentral.AllAccHoldersResult ret =
                ask.askAndWait(new BankingCentral.GetAllAccountHolders(), 10);
        return ret.result();
    }

    public void getAllAccountHoldersDeferred(IDeferredRequest<String> req) {
        this.getBankingCentral().tell(new BankingCentral.GetAllAccountHoldersDeferred(req), ActorRef.noSender());
    }

    public void printAllAccountNamesInAllBanks() {
        this.getBankingCentral().tell(new BankingCentral.PrintAllAccountNamesInAllBanks(),
                ActorRef.noSender());
    }

    public void printBanksByCount(BankCountCollectionDTO bcColl) {
        if (bcColl == null) {
            return;
        }
        bcColl.getBCs().forEach(bc -> {
            IntStream.range(0, bc.getCount()).forEach(i -> {
                printBankByName(bc.getBankName());
            });
        });
    }

    public void printLocalBanks() {
        this.getBankingCentral().tell(new BankingCentral.PrintLocalBanks(), ActorRef.noSender());
    }

    public void printBankBySelection(String bankName) {
        this.getBankingCentral().tell(new BankingCentral.PrintBankBySelection(bankName), ActorRef.noSender());
    }

    public void printBankByName(String bankName) {
        this.getBankingCentral().tell(new BankingCentral.PrintBankByName(bankName), ActorRef.noSender());
    }

    public String broadcastMessage(String message) {
        String systemName = this.getAU().getLocalSystemName();
        final String msg = String.format("From /[%s]: " + "\n*********\n[%s]\n*********\n", systemName,
                message);

        this.getBankingCentral().tell(new BankingCentral.BroadcastMessage(msg), ActorRef.noSender());
        return msg;
    }

    public void doSleepyNoOp(BankCountCollectionDTO bcColl) {
        if (bcColl == null) {
            return;
        }
        bcColl.getBCs().forEach(bc -> {
            ActorSelection bank = getBank(bc.getBankName());
            IntStream.range(0, bc.getCount()).forEach(i -> {
                bank.tell(new AccountHolder.SleepyNoOp(4, 5, 500), ActorRef.noSender());
            });
        });
    }

    private ActorSelection getBank(String bankName) {
        return getAU().getActorNamed(bankName);
    }

    private ActorSelection getBankingCentral() {
        return getAU().getActor(getAU().getLocalSystemName(), BankingCentral.BANKING_CENTRAL_NAME);
    }

    private IFakeControllerDependency getFakeControllerDependency() {
        return this.getDep(IFakeControllerDependency.class);
    }

    @Override
    public List<Class> getDependenciesList() {
        List<Class> ret = new ArrayList<>();
        ret.add(IFakeControllerDependency.class);
        ret.add(BankingAppInitializer.class);
        return ret;
    }

    public static class BanksDTO {
        private List<BankDTO> banks;

        public BanksDTO() {
            this.banks = new ArrayList<>();
        }

        @JsonProperty
        public List<BankDTO> getBanks() {
            return banks;
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("{[");
            this.getBanks().forEach(x -> sb.append(x).append(","));
            sb.append("]}");
            return sb.toString();
        }

        public static class BankDTO {
            private String name;
            private List<String> accountHolders;

            public BankDTO() {
                this.accountHolders = new ArrayList<>();
            }

            @JsonProperty
            public String getName() {
                return name;
            }

            @JsonProperty
            public List<String> getAccountHolders() {
                return accountHolders;
            }

            public String toString() {
                StringBuffer sb = new StringBuffer();
                sb.append("{name: ").append(this.getName()).append(", accountHolders:[");
                this.getAccountHolders().forEach(x -> sb.append(x).append(","));
                sb.append("]");
                return sb.toString();
            }
        }
    }

    public static class BankCountDTO {
        private String bankName;
        private int count;

        public String getBankName() {
            return bankName;
        }

        public void setBankName(String bankName) {
            this.bankName = bankName;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

    public static class BankCountCollectionDTO {
        List<BankCountDTO> bcs;

        public List<BankCountDTO> getBCs() {
            return bcs;
        }

        public void setBcs(List<BankCountDTO> bcs) {
            this.bcs = bcs;
        }
    }
}
