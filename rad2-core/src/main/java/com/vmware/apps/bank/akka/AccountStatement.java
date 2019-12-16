package com.vmware.apps.bank.akka;

import akka.actor.ActorRef;
import akka.actor.Props;

import com.vmware.akka.aspects.ActorMessageHandler;
import com.vmware.akka.common.BaseActorWithTimer;
import com.vmware.akka.util.JobTracker;
import com.vmware.common.utils.PrintUtils;
import com.vmware.ignite.common.RegistryManager;
import com.vmware.ignite.util.JobStatusEnum;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class AccountStatement extends BaseActorWithTimer {
    private static final String TICK_KEY = "Tick_Tick";
    private static final String AH_KEY = "AH";
    private String accountHolderStatement;
    private Map<String, String> accountStatements;
    private ActorRef jobTracker;

    public AccountStatement(RegistryManager rm, ActorRef jobTracker) {
        super(rm);
        this.jobTracker = jobTracker;
        this.accountHolderStatement = null;
        this.accountStatements = new HashMap<>();
        Account.AccountNameEnum.applyToAccountNames(x -> this.accountStatements.put(x, null));
    }

    static public Props props(RegistryManager rm, ActorRef jobTracker) {
        return Props.create(AccountStatement.class, rm, jobTracker);
    }

    @Override
    public Receive createReceive() {
        return super.createReceive()
            .orElse(receiveBuilder()
                .match(ReceiveStatement.class, this::receiveStatement)
                .match(BeginStatementPreparation.class, x -> beginStatementPreparation(x))
                .build());
    }

    private boolean isReady(Tick<String> t) {
        boolean ret = true;
        long accountStatementsNotFilled = this.accountStatements.values()
            .stream().filter(Objects::isNull).count();
        if (this.accountHolderStatement != null &&
            accountStatementsNotFilled > 0) ret = false;
        return ret;
    }

    private void onReady(Tick<String> t) {
        StringBuilder sb = new StringBuilder();
        sb.append(this.accountHolderStatement);
        this.accountStatements.values().forEach(sb::append);
        PrintUtils.printToActor(sb.toString());
        this.getJT().tell(new JobTracker.UpdateJob(JobStatusEnum.JOB_STATUS_SUCCESS),
            self());
    }

    private void onTick(Tick<String> t) {
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

    @ActorMessageHandler
    private void receiveStatement(ReceiveStatement x) {
        ActorRef jt = this.getJT();
        if (x.requestId.equals(AH_KEY)) {
            this.accountHolderStatement = x.statement;
            jt.tell(new JobTracker.UpdateJob(JobStatusEnum.JOB_STATUS_IN_PROGRESS,
                "Collected account holder details"), self());
        } else {
            this.accountStatements.put(x.requestId, x.statement);
            jt.tell(new JobTracker.UpdateJob(JobStatusEnum.JOB_STATUS_IN_PROGRESS,
                "Collected account details"), self());
        }
    }

    @ActorMessageHandler
    private void beginStatementPreparation(BeginStatementPreparation data) {
        // TODO Morph the state machine so no more timers can be started, until this one is done.
        // TODO Else prior timers named TICK_KEY will keep getting overwritten
        // start a periodic timer in order to check on whether the statements that have been
        // requested are ready.
        Consumer<Tick<String>> cons = this::onTick;
        this.startTimer(new Tick(TickTypeEnum.PERIODIC, cons, TICK_KEY, 100, TimeUnit.MILLISECONDS));
        // send this AccountStatement to AH to fill up
        data.ah.tell(new AccountStatement.RequestStatement(AH_KEY), self());
        // for each Account, send this AccountStatement to it to fill up
        data.accounts.keySet()
            .forEach(acName -> data.accounts.get(acName).tell(new AccountStatement.RequestStatement(acName),
                self()));
    }

    private ActorRef getJT() {
        return this.jobTracker;
    }

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
        public final ActorRef jt; // the job tracker to track statement preparation
        public final Map<String, ActorRef> accounts; // the accounts under this AccountHolder

        public BeginStatementPreparation(ActorRef ah, ActorRef jt, Map<String, ActorRef> accounts) {
            this.ah = ah;
            this.jt = jt;
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
