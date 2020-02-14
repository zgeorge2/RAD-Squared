package com.rad2.apps.adm.akka;

import akka.actor.Props;
import com.rad2.akka.common.BaseActorWithTimer;
import com.rad2.ignite.common.RegistryManager;

import java.util.concurrent.TimeUnit;

public class JobTrackerAdmin extends BaseActorWithTimer {
    private static final long TICK_TIME = 60 * 1000; // unit: millis; 1 minutes
    private static final long JOB_RETAIN_TIME = 10 * TICK_TIME; // unit: millis; 1 minutes
    public static String JT_ADMIN_NAME = "JT_ADMIN";

    protected JobTrackerAdmin(RegistryManager rm) {
        super(rm, new Tick(TickTypeEnum.PERIODIC, "JobTrackerAdminPeriodic", TICK_TIME, TimeUnit.MILLISECONDS));
    }

    static public Props props(RegistryManager rm) {
        return Props.create(JobTrackerAdmin.class, rm);
    }

    @Override
    public Receive createReceive() {
        return super.createReceive()
                .orElse(receiveBuilder()
                        .build());
    }

    @Override
    protected void onTick(Tick t) {
        // age for staleness is set to the timer tick interval
        getJR().tell(new JobTrackerWorker.CleanUp(getAU().getLocalSystemName(), JOB_RETAIN_TIME), sender());
    }

    /**
     * Classes below for messages to this Actor
     */
}
