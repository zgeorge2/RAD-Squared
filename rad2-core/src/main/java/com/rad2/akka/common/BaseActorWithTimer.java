package com.rad2.akka.common;

import akka.actor.AbstractActorWithTimers;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Props;
import com.rad2.akka.aspects.ActorMessageHandler;
import com.rad2.apps.adm.akka.JobTrackerWorker;
import com.rad2.common.utils.PrintUtils;
import com.rad2.ignite.common.RegistryManager;
import com.rad2.ignite.common.UsesRegistryManager;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * In addition to BaseActor characteristics, this actor also uses Timers
 */
public abstract class BaseActorWithTimer extends AbstractActorWithTimers
        implements UsesRegistryManager, IJobWorkerClient {
    private RegistryManager rm;

    protected BaseActorWithTimer(RegistryManager rm, Tick tick) {
        this.rm = rm;
        PrintUtils.printToActor("CREATED Timer Actor: [%s]@[%s]", this.getClass().getSimpleName(),
                this.self().path());
        startTimer(tick);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(BaseActor.CreateChild.class, this::createChild)
                .match(Tick.class, this::tick)
                .match(Terminate.class, this::terminate)
                .build();
    }

    @ActorMessageHandler
    private ActorRef createChild(BaseActor.CreateChild args) {
        return this.add(args.childPropsSupplier, args.childName);
    }

    public ActorRef add(Supplier<Props> propsSupplier, String name) {
        return this.getAU().add(this.getContext(), propsSupplier, name);
    }

    public RegistryManager getRM() {
        return this.rm;
    }

    public final AkkaActorSystemUtility getAU() {
        return this.getRM().getAU();
    }

    public ActorSelection getJR() {
        return getAU().getActor(getAU().getLocalSystemName(), JobTrackerWorker.JOB_TRACKER_MASTER_ROUTER);
    }

    /**
     * handle the tick
     */
    private void tick(Tick t) {
        this.onTick(t); // delegate back to the handler that was instantiated into the Tick
    }

    /**
     * Override to handle ticks
     */
    protected abstract void onTick(Tick t);

    /**
     * start the timer
     */
    protected final void startTimer(Tick t) {
        if (t.getType() == TickTypeEnum.PERIODIC) {
            this.getTimers().startPeriodicTimer(t.getKey(), t, t.getDuration());
        } else {
            this.getTimers().startSingleTimer(t.getKey(), t, t.getDuration());
        }
    }

    /**
     * cancel the specific timer
     */
    protected final void stopTimer(Tick t) {
        this.getTimers().cancel(t.getKey());
    }

    /**
     * Terminate this timer actor
     */
    protected final void terminate(Terminate t) {
        this.context().stop(self());
    }

    /**
     * Classes for interacting with the AbstractActorWithTimers functionality
     */
    public enum TickTypeEnum {
        PERIODIC("0"), SINGLE("1");
        private String type;

        TickTypeEnum(String type) {
            this.type = type;
        }
    }

    static public class Tick {
        private static final String TICK_KEY_PREFIX = "TT_";
        private final TickTypeEnum tickTypeEnum;
        private final String tickKey; // unique identifier of this tick
        private final FiniteDuration tickDuration; // the finite duration of each Tick

        /**
         * @param tickTypeEnum the type of ticking needed
         * @param tickKey      unique identifier of this Tick message
         * @param tickDuration the duration in tickUnits of this tick
         * @param tickUnit     the unit of the duration
         */
        public Tick(TickTypeEnum tickTypeEnum, String tickKey, long tickDuration, TimeUnit tickUnit) {
            this.tickTypeEnum = tickTypeEnum;
            this.tickKey = TICK_KEY_PREFIX + tickKey;
            this.tickDuration = Duration.create(tickDuration, tickUnit);
        }

        public TickTypeEnum getType() {
            return this.tickTypeEnum;
        }

        public FiniteDuration getDuration() {
            return this.tickDuration;
        }

        public String getKey() {
            return tickKey;
        }

        public String toString() {
            return String.format("Tick<%s>:[Dur = %s][Key=%s]", getType(), getDuration(), getKey());
        }
    }

    static public class Terminate {
    }
}
