package com.vmware.apps.bank.akka;

import akka.actor.Props;
import akka.dispatch.BoundedMessageQueueSemantics;
import akka.dispatch.RequiresMessageQueue;
import com.vmware.akka.aspects.ActorMessageHandler;
import com.vmware.akka.common.BaseActorWithTimer;
import com.vmware.akka.router.WorkerActor;
import com.vmware.akka.router.WorkerClassArgs;
import com.vmware.common.utils.PrintUtils;
import com.vmware.ignite.common.RegistryManager;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Printer extends BaseActorWithTimer implements WorkerActor,
    RequiresMessageQueue<BoundedMessageQueueSemantics> {
    public static final String PRINTER_MASTER_ROUTER_NAME = "pRtr";
    public static final String BANNER_KEY = "BANNER_KEY";
    private String printerId;
    private String banner;

    private Printer(RegistryManager rm, String printerId, String banner) {
        super(rm);
        this.printerId = printerId;
        this.banner = banner;
    }

    static public Props props(WorkerClassArgs args) {
        return Props.create(Printer.class, args.getRM(), args.getId(), args.getArg(BANNER_KEY));
    }

    @Override
    public Receive createReceive() {
        return super.createReceive()
            .orElse(receiveBuilder()
                .match(Print.class, this::print)
                .build());
    }

    @ActorMessageHandler
    private void print(Print p) {
        Consumer<PrintTick> cons = this::onTick;
        PrintTick t = new PrintTick(TickTypeEnum.PERIODIC, cons, 3, p);
        PrintUtils.printToActor("*** [WORKER: %s]: BEGIN  WORKING ON [%s] ***###", getId(), t.getKey());
        // arbitrary sleep before the actual work begins
        try {
            for (int ii = 0; ii < 5; ii++) {
                // Waiting symbol
                PrintUtils.printToActor("[{Thread: %s}:{%s}{Wait: %d}] ",
                    Thread.currentThread().getId(), p.getJobId(), ii);
                Thread.currentThread().sleep(10);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // now begin the pretended wait for some work to get done.
        this.startTimer(t);
    }

    private void onTick(PrintTick t) {
        if (!t.incrementTicks().hasReachedMaxTicks()) {
            // Ticking symbol
            System.out.println(String.format("[{Thread: %s}:{%s}{Tick: %d}] ",
                Thread.currentThread().getId(), t.getKey(), t.getTicksCompleted()));
            return; // It hasn't reached. Hence, continue checking on the timer
        }
        System.out.println();
        // condition to stop is reached
        this.stopTimer(t);
        // get things done since condition is reached
        PrintUtils.printToActor("*** [WORKER: %s]: RESULT: [%s] = [%s] ***###", getId(), t.getKey(),
            t.getMessage());
        PrintUtils.printToActor("*** [WORKER: %s]: END  WORKING ON [%s] ***###", getId(), t.getKey());
        // nothing to terminate, since this is a Routee that will be reused.
    }

    private String getId() {
        return this.printerId;
    }

    /**
     * Each class below represents a statement that can be received by this Actor. Note that these messages
     * are immutable structures. Message handling is done in the "createReceive" method of this Actor class.
     */
    static public class Print {
        public final String message;
        public final String jobId;

        public Print(String message, String jobId) {
            this.message = message;
            this.jobId = jobId;
        }

        public String getMessage() {
            return message;
        }

        public String getJobId() {
            return jobId;
        }

        public String toString() {
            return this.message;
        }
    }

    static public class PrintTick extends Tick<String> {
        private int maxTicks;
        private Print p;
        private int ticksCompleted; // breaking immutability here

        public PrintTick(TickTypeEnum type, Consumer<PrintTick> consumer,
                         int maxTicks, Print p) {
            super(type, consumer, p.getJobId(), 1, TimeUnit.SECONDS);
            this.maxTicks = maxTicks;
            this.p = p;
            this.ticksCompleted = 0;
        }

        public int getTicksCompleted() {
            return ticksCompleted;
        }

        public String getMessage() {
            return this.p.getMessage();
        }

        public PrintTick incrementTicks() {
            this.ticksCompleted++;
            return this;
        }

        public boolean hasReachedMaxTicks() {
            if (this.ticksCompleted >= maxTicks) {
                return true;
            }
            return false;
        }
    }
}
