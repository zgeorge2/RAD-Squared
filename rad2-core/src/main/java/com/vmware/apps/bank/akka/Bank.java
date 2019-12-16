package com.vmware.apps.bank.akka;

import akka.actor.ActorRef;
import akka.actor.Props;
import com.vmware.akka.aspects.ActorMessageHandler;
import com.vmware.akka.common.BaseActorWithRegState;
import com.vmware.akka.common.RegistryStateDTO;
import com.vmware.apps.bank.ignite.AccountHolderRegistry;
import com.vmware.apps.bank.ignite.BankRegistry;
import com.vmware.common.serialization.IAkkaSerializable;
import com.vmware.ignite.common.RegistryManager;

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
                .match(Add.class, this::add)
                .build());
    }

    @ActorMessageHandler
    private void print(Print p) {
        this.context().children().foreach(ahActor -> {
            ahActor.tell(new AccountHolder.Print(), self());
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
    private void getAllAccountHolders(GetAllAccountHolders i) {
        AccountHolderRegistry ahReg = reg(AccountHolderRegistry.class);
        GetAllAccountHoldersResult ret = new GetAllAccountHoldersResult();
        ahReg.applyToChildrenOfParent(this.getRegId(), ah -> ret.add(ah.getName()));
        sender().tell(ret, self()); // send it back to the calling "actor" (or other)
    }

    @ActorMessageHandler
    private void createAccountHolder(CreateAccountHolder cah) {
        String nm = cah.accountHolderName;
        ActorRef ah = this.add(() -> AccountHolder.props(this.getRM(), this.getRegId(), nm), nm);
        ah.tell(new AccountHolder.CreateAccounts(ACCOUNT_STARTING_BALANCE), self());
    }

    @ActorMessageHandler
    private void add(Add add) {
        this.context().children().foreach(c -> {
            c.tell(new AccountHolder.Add(4, 5), self());
            return true;
        });

    }

    /**
     * Classes used for received method above.
     */
    static public class Print implements IAkkaSerializable {
        public String message;

        public Print() {
            this(null);
        }

        public Print(String message) {
            this.message = message == null ? "<NO MESSAGE>" : message;
        }
    }

    static public class CreateAccountHolder {
        public String accountHolderName;

        public CreateAccountHolder(String accountHolderName) {
            this.accountHolderName = accountHolderName;
        }
    }

    static public class GetAllAccountHolders {
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
                sb.append(String.format("[%s]; ", ahName));
            });
            return sb.toString();
        }
    }

    // Added a fake field to avoid the serializable exception.
    static public class Add implements IAkkaSerializable{
        private Long id;
        public Add() {
            this(null);
        }

        public Add(Long id) {
            this.id = id;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }
}

