/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.akka.common;

import akka.actor.AbstractActor;
import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import com.rad2.akka.aspects.ActorMessageHandler;
import com.rad2.ignite.common.UsesRegistryManager;

import java.util.function.Supplier;

/**
 * Represents default Actor behavior that can be availed of in subclasses of Akka Actor classes like
 * AbstractActor or AbstractActorWithTimer.
 */
public interface ExtendedActorBehavior extends UsesRegistryManager {
    default AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(CreateChild.class, this::createChild)
                .match(Terminate.class, this::terminate)
                .build();
    }

    default AkkaActorSystemUtility getAU() {
        return getRM().getAU();
    }

    default ActorRef add(Supplier<Props> propsSupplier, String name) {
        return getAU().add(getContext(), propsSupplier, name);
    }

    @ActorMessageHandler
    default void createChild(BaseActor.CreateChild args) {
        add(args.childPropsSupplier, args.childName);
    }

    /**
     * Terminate this timer actor
     */
    @ActorMessageHandler
    default void terminate(Terminate t) {
        getContext().stop(self());
    }

    /**
     * Classes used to message Actors implementing this interface
     */
    class CreateChild {
        public Supplier<Props> childPropsSupplier;
        public String childName;

        public CreateChild(Supplier<Props> childPropsSupplier, String childName) {
            this.childPropsSupplier = childPropsSupplier;
            this.childName = childName;
        }
    }

    class Terminate {
    }

    /**
     * These are only implemented by actual Akka Actor classes. Hence this interface and its extended interfaces
     * are only used usually by Actor subclasses too.
     */
    ActorRef self();

    ActorContext getContext();

    ReceiveBuilder receiveBuilder();
}

