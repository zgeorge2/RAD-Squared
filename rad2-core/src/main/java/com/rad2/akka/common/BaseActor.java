/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.akka.common;

import akka.actor.AbstractActor;
import com.rad2.common.utils.PrintUtils;
import com.rad2.ignite.common.RegistryManager;

/**
 * This is the minimalistic combination in this module. It is an AbstractActor, and uses a RegistryManager
 */
public abstract class BaseActor extends AbstractActor implements IJobWorkerClient {
    private RegistryManager rm;

    protected BaseActor(RegistryManager rm) {
        this.rm = rm;
        PrintUtils.print("CREATED Actor: [%s]@[%s]", this.getClass().getSimpleName(),
                this.self().path());
    }

    @Override
    public Receive createReceive() {
        return IJobWorkerClient.super.createReceive()
                .orElse(receiveBuilder()
                        .match(Ping.class, this::ping)
                        .build());
    }

    public final RegistryManager getRM() {
        return this.rm;
    }

    private void ping(Ping arg) {
    }

    /**
     * Messages specific to BaseActor
     */
    public static class Ping {
    }
}

