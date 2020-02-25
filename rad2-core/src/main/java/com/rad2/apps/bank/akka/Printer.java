/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.apps.bank.akka;

import akka.actor.Props;
import akka.dispatch.BoundedMessageQueueSemantics;
import akka.dispatch.RequiresMessageQueue;
import com.rad2.akka.aspects.ActorMessageHandler;
import com.rad2.akka.common.BaseActor;
import com.rad2.akka.router.WorkerActor;
import com.rad2.akka.router.WorkerClassArgs;
import com.rad2.common.utils.PrintUtils;
import com.rad2.ignite.common.RegistryManager;

public class Printer extends BaseActor implements WorkerActor,
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
        try {
            for (int ii = 0; ii < 5; ii++) {
                PrintUtils.printToActor("[{Thread: %s}:{%s}[%s]{Wait: %d}] ",
                        Thread.currentThread().getId(), p.getJobId(), p.getMessage(), ii);
                Thread.sleep(10);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
            return message;
        }
    }
}
