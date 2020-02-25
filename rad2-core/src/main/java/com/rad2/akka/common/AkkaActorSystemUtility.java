/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.akka.common;

import akka.actor.*;
import akka.util.Timeout;
import com.rad2.common.utils.PrintUtils;
import com.rad2.ignite.common.DModel;
import com.rad2.ignite.common.RegistryManager;
import com.rad2.ignite.common.SystemConfigRegistry;
import com.rad2.ignite.common.UsesRegistryManager;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Provides access to the various Actors in the application. This can also provide access to Actors in remote
 * systems using the SystemConfigRegistry, which stores configs of all participating systems.
 */
public class AkkaActorSystemUtility implements UsesRegistryManager {
    private static final String DEAD_LETTER_OFFICE_ACTOR_NAME = "dead-letter-office";
    private static final Timeout TIMEOUT = new Timeout(Duration.create(2, TimeUnit.SECONDS));
    private RegistryManager rm;
    private String registryId; // id of the local system in SCReg
    private ActorSystem system;

    public AkkaActorSystemUtility(SystemProperties sysProps, RegistryManager rm) {
        PrintUtils.printToActor("*** Creating AkkaActorSystemUtility ***");
        this.rm = rm;

        // store these details in the SCReg. This will allow other nodes in the cluster to reach this systems
        this.registryId = this.getSCReg().add(sysProps);

        // create the local actor system
        this.system = ActorSystem.create(this.getSCReg().getLocalSystemName(), sysProps.getRawConfig());

        // subscribe for all dead letters
        this.system.eventStream().subscribe(this.add(() -> DeadLetterOffice.props(this.rm),
            DEAD_LETTER_OFFICE_ACTOR_NAME), AllDeadLetters.class);
    }

    /**
     * ensure actor system termination happens gracefully and correctly (see thread launch is needed since
     * caller must NOT block on this method
     */
    public void terminate() {
        PrintUtils.printToActor("Start terminating the Actor System by calling System.exit ...");
        ActorSystem sys = this.getActorSystem();
        // schedule (with zero delay) the system exit. Cannot be done on the calling thread.
        sys.scheduler().scheduleOnce(Duration.Zero(), () -> System.exit(1), sys.dispatcher());
    }

    /**
     * setup a shutdown hook for the actor system to shutdown gracefully
     */
    public void setupActorSystemShutdown() {
        PrintUtils.printToActor("Setting up Actor System shutdown hook ...");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Await.result(getActorSystem().terminate(), Duration.create(15, TimeUnit.SECONDS));
            } catch (Exception e) {
                PrintUtils.printToActor("Failed to Shut down Actor System");
                e.printStackTrace();
            }
        }));
        this.getActorSystem().registerOnTermination(() -> PrintUtils.printToActor("ActorSystem terminated!"));
    }

    /**
     * Add a new Actor instance, that will be created using props. The provided actorPath represents the path
     * to the actor, but is used to locate the parent Actor to whom this new instance is added as a child.
     * Only local system paths are considered. if the path is of a foreign system - then null is returned.
     *
     * @return
     */
    public final ActorRef addAtPath(Supplier<Props> propsSupplier, String actorPath) {
        ActorRef ret = null;
        if (!this.pathIsLocalSystem(actorPath)) {
            return ret;
        }
        String actorName = this.getActorName(actorPath);
        ActorSelection actorParent = this.getActorParent(actorPath);
        if (actorParent == null) {
            PrintUtils.printToActor("Failed to create actor:[%s]. Missing parent actor!", actorPath);
            return ret;
        }
        if (this.pathParentEqualsUser(actorPath)) {
            ret = this.add(propsSupplier, actorName); // this is to be added via ActorSystem
        } else {
            // needs to be added using context of the parent actor
            actorParent.tell(new ExtendedActorBehavior.CreateChild(propsSupplier, actorName), ActorRef.noSender());
            // wait for the creation of the actor to happen. THis is NOT guaranteed. It may timeout
            // and creation may continue in background.
            ret = this.resolveActor(actorPath); // Todo: Need to revisit background Actor creation
        }

        return ret;
    }

    /**
     * Add a new Actor instance, that will be created using props, to the ActorSystem
     *
     * @return
     */
    public final ActorRef add(Supplier<Props> propsSupplier, String name) {
        ActorRef ret;
        try {
            ActorSelection aSel = this.getActor(this.getLocalSystemName(), name);
            ret = Await.result(aSel.resolveOne(TIMEOUT), TIMEOUT.duration());
            PrintUtils.printToActor("Failed to create Actor. [%s] already EXISTS!!", ret.path());
        } catch (Exception e) {
            // Actor doesn't exist
            // TODO: There is no guarantee that the actor would have been found in TIMEOUT on a loaded system
            ret = this.getActorSystem().actorOf(propsSupplier.get(), name);
        }

        return ret;
    }

    /**
     * SleepyNoOp a new Actor instance, that will be created using props, as a child of the ActorContext
     *
     * @return
     */
    public final ActorRef add(ActorContext context, Supplier<Props> propsSupplier, String name) {
        ActorRef ret;
        scala.Option<ActorRef> existingChild = context.child(name);
        if (!existingChild.isDefined()) {
            ret = context.actorOf(propsSupplier.get(), name);
        } else {
            ret = existingChild.get();
            PrintUtils.printToActor("Failed to create Actor. [%s] already EXISTS!!", ret.path());
        }
        return ret;
    }

    /**
     * Return true if a parent actor exists for this actorPath
     *
     * @return
     */
    private ActorRef resolveActorParent(String actorPath) {
        return this.resolveActor(this.getParentPath(actorPath));
    }

    /**
     * Return true if there is an Actor for the path specified
     *
     * @return
     */
    private ActorRef resolveActor(String path) {
        ActorRef ret;
        try {
            ActorSelection aSel = this.getActor(path);
            ret = Await.result(aSel.resolveOne(TIMEOUT), TIMEOUT.duration());
        } catch (Exception e) {
            // Actor doesn't exist // no-op.
            ret = null; // redundant, here for effect
        }

        return ret;
    }

    /**
     * returns the ActorSelection for the given path in the specific system. Note that it is not required that
     * an Actor actually be present in this system. This method simply constructs an ActorSelection and you
     * may end up getting dead letter notifications if you tell this Actor anything.
     *
     * @return
     */
    public final ActorSelection getActor(String systemName, String... pathParts) {
        return this.getActor(this.getSCReg().getPath(registryId, systemName, pathParts));
    }

    /**
     * Scans registry entries at the top level of the registry (immediate children of root) to find the first
     * entry whose name matches the arg. Returns an ActorSelection corresponding to that actor, potentially
     * located in another service node of the cluster. If it is unable to find such an Actor, then it returns
     * an ActorSelection in the local system, which may not necessarily exist either.
     *
     * @return
     */
    public ActorSelection getActorNamed(String name) {
        // get the system name of the model entry (registry entry), which is the parentKey for
        // top level objects, such that it matches the argument
        Function<? extends DModel, ActorSelection> func = m -> (m.getName().equals(name) ?
            this.getActor(m.getParentKey(), name) : null);
        // only do immediate children of registry tree root.
        ActorSelection ret = (ActorSelection) this.getRM().traverseAndFindFirstResult(func);
        return !Objects.isNull(ret) ? ret : this.getActor(this.getLocalSystemName(), name);
    }

    /**
     * returns the ActorSelection for the given path in all registered service nodes in the cluster. Note that
     * it is not required that an Actor actually be present at each of these locations or any of them. this
     * method simply constructs an ActorSelection and you may end up getting dead letter notifications if you
     * tell those Actors anything
     *
     * @return
     */
    public final List<ActorSelection> getActorInAllSystems(String... pathParts) {
        List<ActorSelection> ret = getActorInAllRemoteSystems(pathParts);
        ret.add(this.getActor(this.getSCReg().getPath(registryId, this.getLocalSystemName(), pathParts)));
        return ret;
    }

    /**
     * returns a randomly selected remote system actor corresponding to the path. If none, are found, it
     * returns the local system actor corresponding to the path. Either could be a non-existent actor.
     *
     * @return
     */
    public final ActorSelection getActorInRandomRemoteSystem(String... pathParts) {
        ActorSelection loc =
            this.getActor(this.getSCReg().getPath(registryId, this.getLocalSystemName(), pathParts));
        List<ActorSelection> rems = getActorInAllRemoteSystems(pathParts);
        return (rems.size() > 0 ? rems.get((int) (Math.random() * rems.size())) : loc);
    }

    /**
     * Get the list of Actors matching the path from all Remote Systems
     *
     * @return
     */
    public List<ActorSelection> getActorInAllRemoteSystems(String... pathParts) {
        return this.getRegisteredSystems().getRemoteSystems().stream()
            .map(sysName -> this.getActor(this.getSCReg().getPath(registryId, sysName, pathParts)))
            .collect(Collectors.toList());
    }

    private ActorSelection getActor(String path) {
        return this.getActorSystem().actorSelection(path);
    }

    private ActorSelection getActorParent(String path) {
        if (Objects.isNull(path)) return null;
        return this.getActor(this.getParentPath(path));
    }

    /**
     * Returns true if the actor path specified is a direct child of the user space. e.g.,
     * akka://AkkaLAX/user/Bank1 vs akka://AkkaLAX/user/Bank1/FooUser1
     *
     * @return
     */
    private boolean pathParentEqualsUser(String actorPath) {
        String parentPath = this.getParentPath(actorPath);
        if (Objects.isNull(parentPath)) {
            return false;
        }
        return parentPath.endsWith("/user");
    }

    /**
     * Returns true if the actorPath belongs to the local system, as opposed to another node in the cluster
     *
     * @return
     */
    private boolean pathIsLocalSystem(String actorPath) {
        if (Objects.isNull(actorPath)) {
            return false;
        }
        return actorPath.startsWith(this.getLocalSystemPath());
    }

    private String getParentPath(String path) {
        if (Objects.isNull(path) || path.length() == 0) return null;
        return path.substring(0, path.lastIndexOf('/'));
    }

    private String getActorName(String path) {
        if (Objects.isNull(path) || path.length() == 0) return null;
        return path.substring(path.lastIndexOf('/') + 1);
    }

    public String getLocalSystemName() {
        return this.getSCReg().getLocalSystemName();
    }

    private String getLocalSystemPath() {
        return this.getSCReg().getLocalSystemPath(registryId);
    }

    private SystemConfigRegistry.RegisteredSystems getRegisteredSystems() {
        return this.getSCReg().getAllSystems();
    }

    private ActorSystem getActorSystem() {
        return this.system;
    }

    private SystemConfigRegistry getSCReg() {
        return reg(SystemConfigRegistry.class);
    }

    @Override
    public RegistryManager getRM() {
        return this.rm;
    }
}
