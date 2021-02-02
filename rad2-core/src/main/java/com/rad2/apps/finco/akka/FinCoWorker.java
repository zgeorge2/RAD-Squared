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
import com.rad2.apps.finco.ignite.FCAccountRegistry;
import com.rad2.apps.finco.ignite.FinCoRegistry;
import com.rad2.common.utils.Pair;
import com.rad2.common.utils.PrintHeader;
import com.rad2.ignite.common.RegistryManager;

import java.util.List;
import java.util.function.Function;

public class FinCoWorker extends BaseActor implements WorkerActor,
        RequiresMessageQueue<BoundedMessageQueueSemantics> {
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

    private FCAccountRegistry getACCReg() {
        return reg(FCAccountRegistry.class);
    }

    @Override
    public Receive createReceive() {
        return super.createReceive()
                .orElse(receiveBuilder()
                        .match(AddFinCos.class, this::addFinCoList)
                        .match(AddAccountHolders.class, this::addAccountHolders)
                        .match(GetAllBranches.class, this::getAllBranches)
                        .match(GetAllAccountHolders.class, this::getAllAccountHolders)
                        .match(GetAllAccounts.class, this::getAllAccounts)
                        .build());
    }

    @ActorMessageHandler
    private void addFinCoList(AddFinCos arg) {
        // create the registry entry for each finco
        arg.getFinCos().forEach(fc -> {
            String fcRegId =
                    getFCReg().add(new FinCoRegistry.FinCoRegistryStateDTO(fc.getName(), fc.getBranch()));
            addAccountHoldersHelper(fcRegId, fc.getAccountHolders());
        });
    }

    @ActorMessageHandler
    private void addAccountHolders(AddAccountHolders arg) {
        // create the registry entry for each finco
        arg.getFinCoByIdList().forEach(fc -> addAccountHoldersHelper(fc.getFinCoId(), fc.getAccountHolders()));
        updateJobSuccess(arg, "ADDED ALL ACCOUNTS");
    }

    @ActorMessageHandler
    private void getAllBranches(GetAllBranches arg) {
        StringBuffer sb = new StringBuffer();
        PrintHeader ph = new PrintHeader(
                new Pair<>("Fin Corp", 30),
                new Pair<>("Branch", 15),
                new Pair<>("Key", 30));
        sb.append(ph);
        Function<FinCoRegistry.D_FC_FinCo, Boolean> func = (m) -> {
            sb.append(String.format(ph.format(), m.getParentKey(), m.getName(), m.getKey()));
            return true;
        };
        this.getFCReg().applyToAll(func);
        updateJobSuccess(arg, sb.toString());
    }

    @ActorMessageHandler
    private void getAllAccountHolders(GetAllAccountHolders arg) {
        StringBuffer sb = new StringBuffer();
        PrintHeader ph = new PrintHeader(
                new Pair<>("Fin Corp/Branch", 30),
                new Pair<>("Account Holder", 30),
                new Pair<>("Points", 15),
                new Pair<>("Key", 30));
        sb.append(ph);
        Function<FCAccountHolderRegistry.D_FC_AccountHolder, Boolean> func = (m) -> {
            sb.append(String.format(ph.format(), m.getParentKey(), m.getName(), m.getRewardPoints(), m.getKey()));
            return true;
        };
        this.getAHReg().applyToAll(func);
        updateJobSuccess(arg, sb.toString());
    }

    @ActorMessageHandler
    private void getAllAccounts(GetAllAccounts arg) {
        StringBuffer sb = new StringBuffer();
        PrintHeader ph = new PrintHeader(
                new Pair<>("Fin Corp/Branch/Holder", 30),
                new Pair<>("Type", 15),
                new Pair<>("Account Num", 45),
                new Pair<>("Balance", 15),
                new Pair<>("Key", 50));
        sb.append(ph);
        Function<FCAccountRegistry.D_FC_Account, Boolean> func = (m) -> {
            sb.append(String.format(ph.format(), m.getParentKey(), m.getType(), m.getAccountNumber(), m.getBalance(), m.getKey()));
            return true;
        };
        this.getACCReg().applyToAll(func);
        updateJobSuccess(arg, sb.toString());
    }

    private void addAccountHoldersHelper(String fcRegId, List<FCData.FCAccHol> accountHolders) {
        // use the fcRegId of the finco as parent key for each account holder
        accountHolders.forEach(ah -> {
            String ahRegId = getAHReg().add(new FCAccountHolderRegistry.FCAccountHolderRegistryStateDTO(fcRegId, ah.getName()));
            // use the fcRegId of the finco as parent key for each account holder
            ah.getAccounts().forEach(acc -> getACCReg().createAccount(ahRegId, acc.getType(), acc.getBalance()));
        });
    }

    /**
     * Classes used for receive method above.
     */
    static public class AddFinCos {
        List<FCData.FinCo> finCos;

        public AddFinCos(FCData.FinCoList fcList) {
            this.finCos = fcList.getFinCoList();
        }

        public List<FCData.FinCo> getFinCos() {
            return this.finCos;
        }
    }

    /**
     * Base class for deferred FinCo requests. Must specify the name as req
     * args
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

    static public class GetAllAccounts extends FinCoRequest {
        public GetAllAccounts(IDeferred<String> req) {
            super(req);
        }
    }

    static public class AddAccountHolders extends BasicDeferredMessage<String> {
        public static final String FC_BY_ID_LIST_KEY = "FC_BY_ID_LIST_KEY";

        public AddAccountHolders(IDeferred<String> req) {
            super(req);
        }

        public List<FCData.FinCoById> getFinCoByIdList() {
            return ((FCData.FinCoByIdList) arg(FC_BY_ID_LIST_KEY)).getFinCoByIdList();
        }
    }
}

