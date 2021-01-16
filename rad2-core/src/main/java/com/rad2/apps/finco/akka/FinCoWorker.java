/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.apps.finco.akka;

import akka.actor.Props;
import akka.dispatch.BoundedMessageQueueSemantics;
import akka.dispatch.RequiresMessageQueue;
import com.rad2.akka.aspects.ActorMessageHandler;
import com.rad2.akka.common.BaseActor;
import com.rad2.akka.common.BasicDeferredMessage;
import com.rad2.akka.common.IDeferred;
import com.rad2.akka.router.WorkerActor;
import com.rad2.akka.router.WorkerClassArgs;
import com.rad2.apps.finco.ignite.FCAccountHolderRegistry;
import com.rad2.apps.finco.ignite.FinCoRegistry;
import com.rad2.ignite.common.RegistryManager;

import java.util.*;
import java.util.function.Function;

public class FinCoWorker extends BaseActor implements WorkerActor,
        RequiresMessageQueue<BoundedMessageQueueSemantics> {
    private static final int ACCOUNT_STARTING_BALANCE = 100;
    public static final String FINCO_MASTER_ROUTER = "FinCoMaster";
    public static final String BANNER_KEY = "FINCO_BANNER_KEY";
    private String id; // the id of this routee.
    private String banner; // an arbitrary placeholder arg - used for nothing

    private FinCoWorker(RegistryManager rm, String id, String banner) {
        super(rm);
        this.id = id;
        this.banner = banner;
    }

    static public Props props(WorkerClassArgs args) {
        return Props.create(FinCoWorker.class, args.getRM(), args.getId(),
                args.getArg(BANNER_KEY));
    }

    private FinCoRegistry getFCReg() {
        return reg(FinCoRegistry.class);
    }

    private FCAccountHolderRegistry getAHReg() {
        return reg(FCAccountHolderRegistry.class);
    }

    @Override
    public Receive createReceive() {
        return super.createReceive()
                .orElse(receiveBuilder()
                        .match(AddFinCo.class, this::addFinCo)
                        .match(GetAllBranches.class, this::getAllBranches)
                        .build());
    }

    @ActorMessageHandler
    private void addFinCo(AddFinCo arg) {
        // create the registry entry for each finco
        String regId = getFCReg().add(new FinCoRegistry.FinCoRegistryStateDTO(arg.name, arg.branch));
        // use the regId of the finco as parent key for each account holder
        //registry entry
        arg.accountHolders.forEach(ah -> {
            getAHReg().add(new FCAccountHolderRegistry.FCAccountHolderRegistryStateDTO(regId, ah));
        });
    }

    @ActorMessageHandler
    private void getAllBranches(GetAllBranches arg) {
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("\n\t%30s %15s %30s\n", "Fin Corp", "Branch", "Key "));
        sb.append(String.format("\t%30s %15s %30s\n",
                String.join("", Collections.nCopies(30, "-")),
                String.join("", Collections.nCopies(15, "-")),
                String.join("", Collections.nCopies(30, "-"))));
        Function<FinCoRegistry.D_FC_FinCo, Boolean> func = (m) -> {
            sb.append(String.format("\t%s\n", m));
            return true;
        };
        this.getFCReg().applyToAll(func);
        updateJobSuccess(arg, sb.toString());
    }

    /**
     * Classes used for receive method above.
     */
    static public class AddFinCo {
        String name;
        String branch;
        List<String> accountHolders;

        public AddFinCo(String name, String branch,
                        List<String> accountHolders) {
            this.name = name;
            this.branch = branch;
            this.accountHolders = accountHolders;
        }
    }

    /**
     * Base class for most FinCo requests. Must specify the bank name and bank
     * branch as req args
     */
    static public class FinCoRequest extends BasicDeferredMessage<String> {
        public static final String FINCO_NAME_KEY = "FINCO_NAME_KEY";

        public FinCoRequest(IDeferred<String> req) {
            super(req);
        }

        public String name() {
            return (String) arg(FINCO_NAME_KEY);
        }
    }

    static public class FinCoResult {
        String name;

        public FinCoResult(String name) {
            this.name = name;
        }

        public String name() {
            return name;
        }
    }

    static public class GetAllBranches extends FinCoRequest {
        public GetAllBranches(IDeferred<String> req) {
            super(req);
        }
    }

    static public class GetAllAccountHolders extends FinCoRequest {
        public GetAllAccountHolders(IDeferred<String> req) {
            super(req);
        }
    }

    static public class GetAllAccountHoldersResult extends FinCoResult {
        Map<String, List<String>> branchToAccountHolders;

        public GetAllAccountHoldersResult(String name) {
            super(name);
            this.branchToAccountHolders = new HashMap<>();
        }

        public GetAllAccountHoldersResult add(String branch, String ahName) {
            getAHNames(branch).add(ahName);
            return this;
        }

        private List<String> getAHNames(String branch) {
            return this.branchToAccountHolders.computeIfAbsent(branch, k -> new ArrayList<>());
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            branchToAccountHolders.forEach((br, ahNames) -> {
                ahNames.forEach(ah -> {
                    sb.append(String.format("*** [%s/%s][AH:%s] ***, ", name()
                            , br, ah));
                });
            });
            return sb.toString();
        }
    }
}

