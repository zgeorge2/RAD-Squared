package com.rad2.apps.bank.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Props;
import com.rad2.akka.aspects.ActorMessageHandler;
import com.rad2.akka.common.BaseActor;
import com.rad2.akka.common.BasicDeferredMessage;
import com.rad2.akka.common.IDeferred;
import com.rad2.apps.bank.akka.Bank.GetAllAccountHoldersResult;
import com.rad2.apps.bank.ignite.AccountHolderRegistry;
import com.rad2.apps.bank.ignite.BankRegistry;
import com.rad2.common.serialization.IAkkaSerializable;
import com.rad2.common.utils.PrintUtils;
import com.rad2.ignite.common.RegistryManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BankingCentral extends BaseActor {
    public static String BANKING_CENTRAL_NAME = "BANKING_CENTRAL_NAME";

    private BankingCentral(RegistryManager rm) {
        super(rm);
    }

    static public Props props(RegistryManager rm) {
        return Props.create(BankingCentral.class, rm);
    }

    @Override
    public Receive createReceive() {
        return super.createReceive()
                .orElse(receiveBuilder()
                        .match(AccrueMonthlyBankInterestInLocalBanks.class,
                                this::accrueMonthlyBankInterestInLocalBanks)
                        .match(RewardBanksInSender.class, this::rewardBanksInSender)
                        .match(PrintLocalBanks.class, this::printLocalBanks)
                        .match(PrintBankBySelection.class, this::printBankBySelection)
                        .match(PrintBankByName.class, this::printBankByName)
                        .match(GetAllAccountHolders.class, this::getAllAccountHoldersOfAllBanks)
                        .match(Bank.GetAllAccountHolders.class, this::getAllAccountHoldersOfBank)
                        .match(InitiateFibonacci.class, this::initiateFibonacci)
                        .match(GenerateNextFibonacci.class, this::generateNextFibonacci)
                        .match(PrintFibonacci.class, this::printFibonacci)
                        .match(BroadcastMessage.class, this::broadcastMessage)
                        .match(ReceiveBroadcastMessage.class, this::receiveBroadcastMessage)
                        .build());
    }

    @ActorMessageHandler
    private void accrueMonthlyBankInterestInLocalBanks(AccrueMonthlyBankInterestInLocalBanks i) {
        // send a message to each bank to accrue interest.
        getAllLocalBanks().forEach(b -> getBank(b).tell(new Account.AccrueInterest(), self()));
    }

    @ActorMessageHandler
    private void rewardBanksInSender(RewardBanksInSender arg) {
        // send a message to each bank to accrue extra rewards in the form of interest.
        getAllLocalBanks().forEach(b -> getBank(b).tell(new AccountHolder.AccrueRewardPoints(), self()));
    }

    @ActorMessageHandler
    private void printLocalBanks(PrintLocalBanks arg) {
        getAllLocalBanks().forEach(bn -> {
            Bank.BankRequest req = new Bank.BankRequest(arg);
            req.putArg(Bank.BankRequest.BANK_NAME_KEY, bn);
            printBank(req);
        });
    }

    @ActorMessageHandler
    private void printBankByName(PrintBankByName arg) {
        printBank(arg);
    }

    @ActorMessageHandler
    private void printBankBySelection(PrintBankBySelection arg) {
        getBankBySelection(arg.bankName()).forEach(bn -> {
            Bank.BankRequest req = new Bank.BankRequest(arg);
            req.putArg(Bank.BankRequest.BANK_NAME_KEY, bn);
            printBank(req);
        });
    }

    private void printBank(Bank.BankRequest arg) {
        getBank(arg.bankName()).tell(new Bank.Print(arg), self());
    }

    @ActorMessageHandler
    private void getAllAccountHoldersOfBank(Bank.GetAllAccountHolders arg) {
        getBank(arg.bankName()).tell(arg, self());
    }

    @ActorMessageHandler
    private void getAllAccountHoldersOfAllBanks(GetAllAccountHolders arg) {
        try {
            Thread.sleep(arg.getWaitTime() * 1000); // fake processing time
            updateJobSuccess(arg, getAllAccountHoldersOfAllBanksResult().toString());
        } catch (InterruptedException e) {
            updateJobFailed(arg);
            e.printStackTrace();
        }
    }

    @ActorMessageHandler
    private void initiateFibonacci(InitiateFibonacci arg) {
        String printer = this.getAU().getLocalSystemName();
        // Print the loop # started to the node specified in the arg
        String msg = String.format("Started Fib loop#[%d]", arg.numLoops);
        ActorSelection printingBC = this.getAU().getActor(printer, BANKING_CENTRAL_NAME);
        printingBC.tell(new BankingCentral.PrintFibonacci(this.sender(), self(), msg), self());
        // Start generating fibs
        self().tell(new BankingCentral.GenerateNextFibonacci(printer, arg.senderRewards, arg.numLoops),
                self());
    }

    @ActorMessageHandler
    private void generateNextFibonacci(GenerateNextFibonacci arg) {
        ActorSelection rBC = this.getAU().getActorInRandomRemoteSystem(BANKING_CENTRAL_NAME);
        ActorSelection printingBC = this.getAU().getActor(arg.systemForPrinting, BANKING_CENTRAL_NAME);
        if (arg.hasReachedMax()) {
            if (arg.hasMoreLoops()) {
                rBC.tell(new BankingCentral.InitiateFibonacci(arg.senderRewards, arg.numLoops - 1), self());
            }
            // Print the "final" (based on reaching max in this loop) arg fib received to the node
            // specified in the arg
            printingBC.tell(new BankingCentral.PrintFibonacci(this.sender(), self(), arg), self());

            // Print the loop # completed to the node specified in the arg
            String msg = String.format("Completed Fib loop#[%d]", arg.numLoops);
            printingBC.tell(new BankingCentral.PrintFibonacci(this.sender(), self(), msg), self());
            return;
        }
        // Reward all the banks in the sender system with points
        IntStream.range(0, arg.senderRewards)
                .forEach(i -> sender().tell(new RewardBanksInSender(), self()));

        // Generate the next fib in sequence and pass to random remote system
        rBC.tell(new BankingCentral.GenerateNextFibonacci(arg), self());
    }

    @ActorMessageHandler
    private void printFibonacci(PrintFibonacci arg) {
        PrintUtils.printToActor("%s", arg);
    }

    /**
     * Send the message to an random remote system
     */
    @ActorMessageHandler
    private void broadcastMessage(BroadcastMessage arg) {
        ActorSelection rbc = this.getAU().getActorInRandomRemoteSystem(BANKING_CENTRAL_NAME);
        rbc.tell(new BankingCentral.ReceiveBroadcastMessage(arg.message), self());
    }

    /**
     * Receive a broadcast message and print it.
     */
    @ActorMessageHandler
    private void receiveBroadcastMessage(ReceiveBroadcastMessage arg) {
        PrintUtils.printToActor("Received Broadcast [%s] from [%s]", arg.message, sender());
    }

    @NotNull
    private GetAllAccountHoldersResult getAllAccountHoldersOfAllBanksResult() {
        AccountHolderRegistry ahr = reg(AccountHolderRegistry.class);
        GetAllAccountHoldersResult ret = new GetAllAccountHoldersResult();
        Function<AccountHolderRegistry.DAccountHolder, Boolean> func = (ah) -> {
            ret.add(ah.getName());
            return true;
        };
        ahr.applyToAll(func);

        return ret;
    }

    @NotNull
    private List<String> getAllLocalBanks() {
        BankRegistry br = reg(BankRegistry.class);
        List<String> bankNames = new ArrayList<>();
        Function<BankRegistry.DBank, Boolean> func = banksSelectorFunction(bankNames);
        // get the list of local bank names only
        br.applyToChildrenOfParent(this.getAU().getLocalSystemName(), func);
        return bankNames;
    }

    @NotNull
    private List<String> getAllBanks() {
        BankRegistry br = reg(BankRegistry.class);
        List<String> bankNames = new ArrayList<>();
        Function<BankRegistry.DBank, Boolean> func = banksSelectorFunction(bankNames);
        // get the list of Banks (some of which may be remote).
        br.applyToAll(func);
        return bankNames;
    }

    @NotNull
    private ActorSelection getBank(String bankName) {
        return getAU().getActorNamed(bankName);
    }

    @NotNull
    private List<String> getBankBySelection(String bankName) {
        BankRegistry br = reg(BankRegistry.class);
        List<String> bankNames = new ArrayList<>();
        Function<BankRegistry.DBank, Boolean> func = banksSelectorFunction(bankNames);
        // get the list of local banks filtered by the query
        br.applyToFiltered(func, "select_bank", this.getAU().getLocalSystemName(), bankName);
        return bankNames;
    }

    @NotNull
    private Function<BankRegistry.DBank, Boolean> banksSelectorFunction(List<String> banks) {
        return (BankRegistry.DBank d) -> {
            banks.add(d.getName());
            return true;
        };
    }

    @NotNull
    private List<ActorSelection> convert(List<String> bankNames) {
        // convert to Actors
        return bankNames.stream().map(this::getBank).collect(Collectors.toList());
    }

    /**
     * Classes for messages
     */
    static public class AccrueMonthlyBankInterestInLocalBanks {
    }

    static public class RewardBanksInSender implements IAkkaSerializable {
        String sender;

        public RewardBanksInSender() {
            this("<NO SENDER>");
        }

        public RewardBanksInSender(String sender) {
            this.sender = sender;
        }
    }

    static public class PrintLocalBanks extends BasicDeferredMessage<String> {
        public PrintLocalBanks(IDeferred<String> req) {
            super(req);
        }
    }

    static public class PrintBankBySelection extends Bank.BankRequest {
        public PrintBankBySelection(IDeferred<String> req) {
            super(req);
        }
    }

    static public class PrintBankByName extends Bank.BankRequest {
        public PrintBankByName(IDeferred<String> req) {
            super(req);
        }
    }

    static public class GetAllAccountHolders extends BasicDeferredMessage<String> {
        public static final String WAIT_TIME_KEY = "WAIT_TIME_KEY";// just for testing out arbitrary processing time

        public GetAllAccountHolders(IDeferred<String> req) {
            super(req);
        }

        public long getWaitTime() {
            return (long) arg(WAIT_TIME_KEY);
        }
    }

    static public class BroadcastMessage {
        String message;

        public BroadcastMessage(String message) {
            this.message = message;
        }
    }

    static public class ReceiveBroadcastMessage extends BroadcastMessage implements IAkkaSerializable {
        public ReceiveBroadcastMessage(String message) {
            super(message);
        }
    }

    static public class InitiateFibonacci implements IAkkaSerializable {
        int senderRewards; // the number of reward calls to make to the sender
        int numLoops; // number of loops of fibonacci generation to run

        public InitiateFibonacci() {
            this(0, 0);
        }

        public InitiateFibonacci(int senderRewards, int numLoops) {
            this.senderRewards = senderRewards;
            this.numLoops = numLoops;
        }
    }

    static public class PrintFibonacci implements IAkkaSerializable {
        String msg;

        public PrintFibonacci() {
            this.msg = null;
        }

        public PrintFibonacci(ActorRef sender, ActorRef receiver, GenerateNextFibonacci fib) {
            this.msg = String.format("*** Fib:[%s][%d + %d]=[%s][%d] ***",
                    sender, fib.n_1, fib.n, receiver, fib.nFib);
        }

        public PrintFibonacci(ActorRef sender, ActorRef receiver, String message) {
            this.msg = String.format("*** Fib:[%s]:[%s][Msg:%s] ***", sender, receiver, message);
        }

        public String toString() {
            return this.msg;
        }
    }

    static public class GenerateNextFibonacci implements IAkkaSerializable {
        long n_1;
        long n;
        long nFib;
        String systemForPrinting;
        int senderRewards; // the number of reward calls to make to the sender
        int numLoops; // the number of times to reset and start the sequence generation

        public GenerateNextFibonacci() {
            this("", 100, 1);
        }

        public GenerateNextFibonacci(String systemForPrinting, int senderRewards, int numLoops) {
            this.n_1 = 0;
            this.n = 1;
            this.nFib = gen();
            this.systemForPrinting = systemForPrinting;
            this.senderRewards = senderRewards;
            this.numLoops = numLoops;
        }

        public GenerateNextFibonacci(GenerateNextFibonacci prev) {
            this.n_1 = prev.n;
            this.n = prev.nFib;
            this.nFib = gen();
            this.systemForPrinting = prev.systemForPrinting;
            this.senderRewards = prev.senderRewards;
            this.numLoops = prev.numLoops;
        }

        public boolean hasReachedMax() {
            return this.nFib >= Long.MAX_VALUE || this.nFib < 0;
        }

        public boolean hasMoreLoops() {
            return this.numLoops > 0;
        }

        private long gen() {
            return this.n_1 + this.n;
        }
    }
}

