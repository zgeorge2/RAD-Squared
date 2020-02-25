/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.ignite.common;

import akka.actor.ActorRef;
import akka.actor.Props;
import com.rad2.akka.common.AkkaActorSystemUtility;
import com.rad2.akka.common.SystemProperties;
import com.rad2.common.collection.INAryTreeNodeData;
import com.rad2.common.collection.NAryTreeNode;
import com.rad2.common.utils.PrintUtils;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Every subclass of BaseModelRegistry will likely need access to other BaseModelRegistry subclasses,
 * sometimes to a peer, to a child, to a parent, etc. The RegistryManager holds references to every Registry
 * instance that is a child of BaseModelRegistry and is injected, along with the Ignite Provider at the
 * BaseModelRegistry level itself. Thus every subclass has access to every other BaseModelRegistry subclass
 * and to the Ignite framework through this class. All BaseModelRegistry subclasses are expected to be
 * singletons. The various sub classes of BaseModelRegistry provide access to various IgniteRegistry's. Each
 * sub class is likely to depend on another BaseModelRegistry instance (e.g. the BankRegistry may need to use
 * the AccountHolderRegistry (parent to child), the AccountTransactionRegistry may need access to the
 * AccountHolderRegistry (child to parent), etc.). The RegistryManager then acts as a Composite of all
 * BaseModelRegistry instances and provides access to them.
 */
public class RegistryManager<T extends BaseModelRegistry> {
    public static final String ROOT_NODE_NAME = "root_registry";
    private AkkaActorSystemUtility au;
    private Map<String, T> regMap;
    private NAryTreeNode<T> regsForActors; // the root node
    private IgniteProvider igniteProvider;

    public RegistryManager(SystemProperties sysProps, List<T> regList) {
        PrintUtils.printToActor("*** Creating RegistryManager ***");
        this.igniteProvider = new IgniteProvider(sysProps);
        // Registry entries may need Ignite - so create that above first.
        this.regMap = new HashMap<>();
        // add each registry list item into the registry manager
        regList.forEach(reg -> {
            // each BaseModelRegistry has access to every other BaseModelRegistry via the
            // RegistryManager. Hence need to initialize the BaseModelRegistry with the RM.
            reg.initialize(this);
            this.regMap.put(reg.getClass().getSimpleName(), reg);
        });
        // Create the root node of the registry NAryTree that has all child registries that keep actor state.
        this.regsForActors = new NAryTreeNode<>(ROOT_NODE_NAME, null);
        // also add the registry entries into the regsForActors
        this.regsForActors.addChildren(this.regMap.values()
            .stream()
            .map(regEntry -> (INAryTreeNodeData) regEntry).collect(Collectors.toList()));
        this.regsForActors.traverseBreadthFirst(regN -> {
            PrintUtils.printToActor("Adding Reg Tree Node[%s]", regN.getPath());
            return true;
        });
        // create the actor utility class AFTER the reg manager is almost wholly constructed.
        this.au = new AkkaActorSystemUtility(sysProps, this);
        this.initializeActorsFromRegistry(); // after this.au is created.
    }

    public T get(Class regClass) {
        return this.regMap.get(regClass.getSimpleName());
    }

    /**
     * Shuts down the registry
     */
    public boolean shutdownRegistry() {
        return this.getIgniteProvider().shutdown();
    }

    /**
     * Traverses the registries that are the direct children of the root, applies the function to each
     * registry entry and returns the first non-null result, else return null
     *
     * @return
     */
    public <K extends DModel, R> R traverseAndFindFirstResult(Function<K, R> func) {
        // only do immediate children of registry tree root.
        Map<String, Map<K, R>> map = this.traverseAndApply(func);
        Optional<Map<K, R>> findFirstMap = map.values().stream().findFirst();
        return findFirstMap.isPresent() ? findFirstMap.get().values().stream().findFirst().get() : null;
    }

    /**
     * Traverses the registries that are direct children of the root and applies the function to each registry
     * entry. Returns a map of the registry name to the results. Each such result is a map of registry entry
     * key to result of applying the function to that entry. Null valued results are filtered out.
     *
     * @return
     */
    public <K extends DModel, R> Map<String, Map<K, R>> traverseAndApply(Function<K, R> func) {
        return this.traverseAndApply(func, (e -> e.getValue() != null), 1);
    }

    /**
     * Traverses the registries under root and applies the function to each registry entry. The traversal is
     * restricted by maxLevel. Returns a map of the registry name to the results. Each such result is a map of
     * registry entry key to result of applying the function to that entry. The predicate is applied to each
     * registry entry and only those that pass are returned in the final result.Use sparingly, as
     * unconstrained, this traverses every entry in every registry.
     *
     * @return
     */
    public <K extends DModel, R> Map<String, Map<K, R>> traverseAndApply(Function<K, R> func,
                                                                         Predicate<Map.Entry<K, R>> resultFilter,
                                                                         int maxLevel) {
        Map<String, Map<K, R>> ret = new HashMap<>();

        this.regsForActors.traverseBreadthFirst(regN -> {
            T registry = regN.getData();
            if (!Objects.isNull(registry)) {
                // get all the results
                Map<K, R> queryResults = registry.applyToAll(func);
                // filter out any results as per the predicate
                queryResults = queryResults.entrySet().stream()
                    .filter(resultFilter).collect(Collectors.toMap(Map.Entry::getKey,
                        Map.Entry::getValue));
                if (!queryResults.isEmpty()) ret.put(registry.getTreeNodeName(), queryResults);
            }
            return true;
        }, maxLevel);
        return ret;
    }

    public IgniteProvider getIgniteProvider() {
        return this.igniteProvider;
    }

    public AkkaActorSystemUtility getAU() {
        return this.au;
    }

    private <K extends DModel> void initializeActorsFromRegistry() {
        // initialize Actors out of registry entries (if needed)
        Function<K, ActorRef> actorCreationFunc = (K model) -> {
            Class aClass = model.getActorClass();
            String aName = model.getName();
            String aPath = model.getActorPath();
            if (Objects.isNull(aClass) || Objects.isNull(aName) || Objects.isNull(aPath)) {
                return null;
            }
            if (!model.shouldReincarnateActor()) {
                return null;
            }
            return getAU().addAtPath(() -> Props.create(aClass, this, model.toRegistryStateDTO()), aPath);
        };
        this.regsForActors.traverseBreadthFirst(regN -> {
            T registry = regN.getData();
            if (!Objects.isNull(registry)) registry.applyToAll(actorCreationFunc);
            return true;
        });
    }
}