/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.akka.router;

import akka.actor.Props;
import akka.actor.Terminated;
import akka.routing.ActorRefRoutee;
import akka.routing.Routee;
import akka.routing.Router;
import akka.routing.RoutingLogic;
import com.rad2.akka.aspects.ActorMessageHandler;
import com.rad2.akka.common.BaseActor;
import com.rad2.common.serialization.IAkkaSerializable;
import com.rad2.common.utils.PrintUtils;
import com.rad2.ignite.common.RegistryManager;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class MasterRouter extends BaseActor {
    private Router router;
    private AtomicInteger lastId;
    private RoutingLogic routingLogic;
    private int workerCount;
    private Function<WorkerClassArgs, Props> workerCreator;
    private Consumer<WorkerClassArgs> workerArgsEmbellisher;


    public MasterRouter(RegistryManager rm, RoutingLogic routingLogic, int workerCount,
                        Function<WorkerClassArgs, Props> workerCreator,
                        Consumer<WorkerClassArgs> workerArgsEmbellisher) {
        super(rm);
        this.routingLogic = routingLogic;
        this.workerCount = workerCount;
        this.workerCreator = workerCreator;
        this.workerArgsEmbellisher = workerArgsEmbellisher;
        this.lastId = new AtomicInteger(0);
    }
    static public Props props(RegistryManager rm, RoutingLogic routingLogic, int workerCount,
                              Function<WorkerClassArgs, Props> workerCreator,
                              Consumer<WorkerClassArgs> workerArgsEmbellisher) {
        return Props.create(MasterRouter.class, rm, routingLogic, workerCount, workerCreator,
                workerArgsEmbellisher);
    }


    @Override
    public Receive createReceive() {
        return super.createReceive()
                .orElse(receiveBuilder()
                        .match(Initialize.class, this::initialize)
                        .match(Terminated.class, this::terminated)
                        .match(IncreaseRoutees.class, this::increaseRoutees)
                        .match(RemoveRoutees.class, this::removeRoutees)
                        .match(UpdateRoutees.class, this::updateRoutees)
                        .matchAny(this::passItOn)
                        .build());
    }

    @ActorMessageHandler
    private int getRoutees(GetRoutees g) {
        return this.router.routees().size();
    }


    /**
     * pass on the message to the worker
     */
    @ActorMessageHandler
    private void passItOn(Object o) {
        if (this.router == null) {
            PrintUtils.print("PassItOn: No router configured! NOTHING TO DO!!");
            return;
        }
        this.router.route(o, getSender());
    }


    /**
     * when a worker terminates, create another one
     */
    @ActorMessageHandler
    private void terminated(Terminated t) {
        if (this.router == null) {
            PrintUtils.print("Terminated: No router configured! NOTHING TO DO!!");
            return;
        }
        router = router.removeRoutee(t.actor());
        router = router.addRoutee(this.createWorker("T"));
    }


    /**
     * increase the number of routees for this router
     */
    @ActorMessageHandler
    private void increaseRoutees(IncreaseRoutees arg) {
        if (this.router == null) {
            PrintUtils.print("Terminated: No router configured! NOTHING TO DO!!");
            return;
        }
        PrintUtils.print("Inc Routees: in [%s]", this.self().path().toString());
        router = router.addRoutee(this.createWorker("I"));
    }


    /**
     * Remove a routee for this router
     */
    @ActorMessageHandler
    private void removeRoutees(RemoveRoutees arg) {
        if(this.router.routees().size() == 1) {
            PrintUtils.print("Can't remove routee, minimum 1 routee required: in [%s]", this.self().path().toString());
            return;
        }
        PrintUtils.print("Removing a routee: in [%s]", this.self().path().toString());
        Routee routeeToRemove = router.routees().last();
        router = router.removeRoutee(routeeToRemove);
        routeeToRemove.send(new Terminate(), self());
        lastId.decrementAndGet();
    }

    /**
     * Update Routees for this router. We send a final value for routees and this function sends increase/remove routee
     * messages to itself to adjust it's size to given argument
     */
    @ActorMessageHandler
    private void updateRoutees(UpdateRoutees arg) {
        if(arg.finalCount < 1) {
            PrintUtils.print("Final size can't be less than 1, Context: [%s]", this.self().path().toString());
            return;
        }

        int currentSize = this.router.routees().size();

        while(currentSize != arg.finalCount) {
            if(currentSize < arg.finalCount) {
                self().tell(new IncreaseRoutees(), self());
                currentSize++;
            } else {
                self().tell(new RemoveRoutees(), self());
                currentSize--;
            }
        }
    }


    /**
     * Initialize the Master Router - create routees
     */
    @ActorMessageHandler
    private void initialize(Initialize o) {
        this.router = new Router(getRoutingLogic(), createWorkers("I"));
    }


    private List<Routee> createWorkers(String idPrefix) {
        return IntStream.range(0, getWorkerCount())
                .mapToObj(i -> createWorker(idPrefix)).collect(Collectors.toList());
    }


    private ActorRefRoutee createWorker(String idPrefix) {
        // any args other than the id that need to be passed to the Routee, needs to be added to the end
        String id = idPrefix + lastId.incrementAndGet();
        WorkerClassArgs args = new WorkerClassArgs(id, this.getRM());
        this.workerArgsEmbellisher.accept(args); // adds any extra args beyond RM & id
        return new ActorRefRoutee(this.getAU().add(this.getContext(), () -> getWorkerCreator().apply(args),
                id));
    }


    private Function<WorkerClassArgs, Props> getWorkerCreator() {
        return workerCreator;
    }


    private RoutingLogic getRoutingLogic() {
        return routingLogic;
    }


    private int getWorkerCount() {
        return workerCount;
    }


    /**
     * Classes for messages
     */
    static public class Initialize {

    }

    static public class UpdateRoutees implements IAkkaSerializable {
        int finalCount;
        public UpdateRoutees(int finalCount) {
            this.finalCount = finalCount;
        }

        public UpdateRoutees() {}
    }


    static public class IncreaseRoutees implements IAkkaSerializable {
        int inc;
        public IncreaseRoutees() {
            this(0);
        }
        public IncreaseRoutees(int inc) {
            this.inc = inc;
        }
    }


    static public class RemoveRoutees implements IAkkaSerializable {
        int dec;

        public RemoveRoutees() {
            this(0);
        }

        public RemoveRoutees(int dec) {
            this.dec = dec;
        }
    }

    static public class GetRoutees implements IAkkaSerializable {

    }
}
