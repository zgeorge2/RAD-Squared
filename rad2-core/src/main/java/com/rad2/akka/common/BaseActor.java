package com.rad2.akka.common;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.rad2.akka.aspects.ActorMessageHandler;
import com.rad2.common.utils.PrintUtils;
import com.rad2.ignite.common.RegistryManager;
import com.rad2.ignite.common.UsesRegistryManager;

import java.util.function.Supplier;

/**
 * This is the minimalistic combination in this module. It is an AbstractActor, and uses a RegistryManager
 */
public abstract class BaseActor extends AbstractActor implements UsesRegistryManager {
    private RegistryManager rm;

    protected BaseActor(RegistryManager rm) {
        this.rm = rm;
        PrintUtils.printToActor("CREATED Actor: [%s]@[%s]", this.getClass().getSimpleName(),
            this.self().path());
    }

    @Override
    /**
     * Override in child class but attach to this createReceive
     */
    public Receive createReceive() {
        return receiveBuilder()
            .match(CreateChild.class, this::createChild)
            .match(Terminate.class, t -> terminate(t))
            .build();
    }

    @ActorMessageHandler
    private ActorRef createChild(CreateChild args) {
        return this.add(args.childPropsSupplier, args.childName);
    }

    /**
     * Terminate this timer actor
     */
    @ActorMessageHandler
    private final void terminate(Terminate t) {
        this.context().stop(self());
    }

    protected final ActorRef add(Supplier<Props> propsSupplier, String name) {
        return this.getAU().add(this.getContext(), propsSupplier, name);
    }

    public final RegistryManager getRM() {
        return this.rm;
    }

    public final AkkaActorSystemUtility getAU() {
        return this.getRM().getAU();
    }

    /**
     * Classes used for received method above.
     */
    public static class CreateChild {
        public Supplier<Props> childPropsSupplier;
        public String childName;

        public CreateChild(Supplier<Props> childPropsSupplier, String childName) {
            this.childPropsSupplier = childPropsSupplier;
            this.childName = childName;
        }
    }

    static public class Terminate {
    }
}

