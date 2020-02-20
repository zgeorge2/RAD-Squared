package com.rad2.apps.bank.akka;

import akka.actor.ActorRef;
import akka.actor.Props;
import com.rad2.akka.aspects.ActorMessageHandler;
import com.rad2.akka.common.BaseActorWithTimer;
import com.rad2.ctrl.deps.IJobRef;
import com.rad2.ctrl.deps.JobRef;
import com.rad2.ignite.common.RegistryManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class AccountStatement extends BaseActorWithTimer {
    private static final long TICK_TIME = 1000; // unit: millis
    private static final String AH_KEY = "AH";
    private String accountHolderStatement;
    private Map<String, String> accountStatements;
    private IJobRef jr; // to track ALL the operations done via this this AcccountStatement

    public AccountStatement(RegistryManager rm, IJobRef jr) {
        super(rm, new Tick(TickTypeEnum.PERIODIC, jr.regId(), TICK_TIME, TimeUnit.MILLISECONDS));
        this.jr = new JobRef(jr);
        this.accountHolderStatement = null;
        this.accountStatements = new HashMap<>();
        Account.AccountNameEnum.applyToAccountNames(x -> this.accountStatements.put(x, null));
    }

    static public Props props(RegistryManager rm, IJobRef jr) {
        return Props.create(AccountStatement.class, rm, jr);
    }

    @Override
    public Receive createReceive() {
        return super.createReceive()
                .orElse(receiveBuilder()
                        .match(ReceiveStatement.class, this::receiveStatement)
                        .match(BeginStatementPreparation.class, this::beginStatementPreparation)
                        .build());
    }

    private boolean isReady(Tick t) {
        boolean ret = true;
        long accountStatementsNotFilled = this.accountStatements.values()
                .stream().filter(Objects::isNull).count();
        if (this.accountHolderStatement != null && accountStatementsNotFilled > 0) ret = false;
        return ret;
    }

    @Override
    public void onTick(Tick t) {
        if (!isReady(t)) {
            return; // It hasn't reached. Hence, continue checking on the timer
        }
        // condition to stop is reached
        this.stopTimer(t);
        // get things done since condition is reached
        this.onReady(t);
        // terminate the actor as it has completed its task
        self().tell(new Terminate(), self());
    }

    private void onReady(Tick t) {
        StringBuilder sb = new StringBuilder();
        sb.append(this.accountHolderStatement);
        this.accountStatements.values().forEach(sb::append);
        updateJobSuccess(jobRef(), sb.toString());
    }

    @ActorMessageHandler
    private void receiveStatement(ReceiveStatement x) {
        if (x.requestId.equals(AH_KEY)) {
            this.accountHolderStatement = x.statement;
        } else {
            this.accountStatements.put(x.requestId, x.statement);
        }
    }

    @ActorMessageHandler
    private void beginStatementPreparation(BeginStatementPreparation data) {
        // send this AccountStatement to AH to fill up
        data.ah.tell(new AccountStatement.RequestStatement(AH_KEY), self());
        // for each Account, send this AccountStatement to it to fill up
        data.accounts.keySet()
                .forEach(acName -> data.accounts.get(acName).tell(new AccountStatement.RequestStatement(acName),
                        self()));
    }

    private IJobRef jobRef() {
        return this.jr;
    }

    /**
     * Classes below for messages to this Actor
     */

    static public class ReceiveStatement {
        public final String requestId;
        public final String statement;

        public ReceiveStatement(String requestId, String statement) {
            this.requestId = requestId;
            this.statement = statement;
        }
    }

    static public class BeginStatementPreparation {
        public final ActorRef ah; // the AccountHolder for which statement is being prepared
        public final Map<String, ActorRef> accounts; // the accounts under this AccountHolder

        public BeginStatementPreparation(ActorRef ah, Map<String, ActorRef> accounts) {
            this.ah = ah;
            this.accounts = accounts;
        }
    }

    static public class RequestStatement {
        public final String requestId;

        public RequestStatement(String requestId) {
            this.requestId = requestId;
        }
    }
}
