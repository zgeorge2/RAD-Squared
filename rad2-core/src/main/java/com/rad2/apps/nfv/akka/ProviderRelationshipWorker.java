/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.apps.nfv.akka;

import akka.actor.Props;
import akka.dispatch.BoundedMessageQueueSemantics;
import akka.dispatch.RequiresMessageQueue;
import com.rad2.akka.aspects.ActorMessageHandler;
import com.rad2.akka.common.BaseActor;
import com.rad2.akka.common.RegistryStateDTO;
import com.rad2.akka.router.WorkerActor;
import com.rad2.akka.router.WorkerClassArgs;
import com.rad2.apps.nfv.ignite.RelRegistry;
import com.rad2.common.utils.PrintUtils;
import com.rad2.ignite.common.RegistryManager;

import java.util.Collections;
import java.util.function.Function;

public class ProviderRelationshipWorker extends BaseActor implements WorkerActor,
    RequiresMessageQueue<BoundedMessageQueueSemantics> {
    public static final String PROVIDER_RELATIONSHIP_MASTER_ROUTER = "provRelationshipMasterRouter";
    public static final String BANNER_KEY = "PROV_RELATIONSHIP_BANNER_KEY";
    private String id; // the id of this routee.
    private String banner; // an arbitrary placeholder arg - used for nothing

    private ProviderRelationshipWorker(RegistryManager rm, String id, String banner) {
        super(rm);
        this.id = id;
        this.banner = banner;
    }

    static public Props props(WorkerClassArgs args) {
        return Props.create(ProviderRelationshipWorker.class, args.getRM(), args.getId(),
            args.getArg(BANNER_KEY));
    }

    private RelRegistry getRelReg() {
        return reg(RelRegistry.class);
    }

    @Override
    public Receive createReceive() {
        return super.createReceive()
            .orElse(receiveBuilder()
                .match(AddRelationship.class, this::addRelationship)
                .match(ListRelationships.class, this::listRelationships)
                .match(ReserveBW.class, this::reserveBW)
                .match(ReturnBW.class, this::returnBW)
                .match(ReturnBWByRegId.class, this::returnBWByRegId)
                .match(ResetRelationship.class, this::resetRelationship)
                .build());
    }

    @ActorMessageHandler
    private void addRelationship(AddRelationship arg) {
        RegistryStateDTO dto = new RelRegistry.RelRegDTO(arg.parentKey, arg.getName(),
            arg.dcReg1, arg.dcReg2, arg.maxBW);
        reg(dto).add(dto);
        PrintUtils.printToActor("****** Created Host [%s]: [%s] ******", self().path().toString(), dto);
    }

    @ActorMessageHandler
    private void listRelationships(ListRelationships arg) {
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("\n\t%45s %30s %30s %15s %15s\n", "Namespace/Rel Id", "Datacenter 1",
            "Datecenter 2", "Max BW", "Avail BW"));
        sb.append(String.format("\t%45s %30s %30s %15s %15s\n",
            String.join("", Collections.nCopies(45, "-")),
            String.join("", Collections.nCopies(30, "-")),
            String.join("", Collections.nCopies(30, "-")),
            String.join("", Collections.nCopies(15, "-")),
            String.join("", Collections.nCopies(15, "-"))));
        Function<RelRegistry.D_NFV_RelModel, Boolean> func = (m) -> {
            sb.append(String.format("\t%s\n", m));
            return true;
        };
        this.getRelReg().applyToAll(func);
        PrintUtils.printToActor("Worker:[%s]", self().path().toString());
        PrintUtils.printToActor("%s", sb.toString());
    }

    @ActorMessageHandler
    private void reserveBW(ReserveBW arg) {
        String regId = arg.getRegId();
        RelRegistry.D_NFV_RelModel model = getRelReg().reserveBW(regId, arg.bw);
        PrintUtils.printToActor("****** Reserved Bandwidth using Worker [%s] from Relationship: [%s] ******",
            self().path().toString(), model);
    }

    @ActorMessageHandler
    private void returnBW(ReturnBW arg) {
        String regId = arg.getRegId();
        RelRegistry.D_NFV_RelModel model = getRelReg().returnBW(regId, arg.bwReturned);
        PrintUtils.printToActor("****** Returned Bandwidth using Worker [%s] for Relationship: [%s] ******",
            self().path().toString(), model);
    }

    @ActorMessageHandler
    private void returnBWByRegId(ReturnBWByRegId arg) {
        RelRegistry.D_NFV_RelModel model = getRelReg().returnBW(arg.regId, arg.bw);
        PrintUtils.printToActor("****** Returned Bandwidth using Worker [%s] for Relationship: [%s] ******",
            self().path().toString(), model);
    }

    @ActorMessageHandler
    private void resetRelationship(ResetRelationship arg) {
        this.getRelReg().resetRelationship(arg.parentKey);
    }

    /**
     * Classes used for receive method above.
     */
    static class RelModifier {
        String parentKey; // think of this as the namespace to which this relationship belongs
        String dcReg1;
        String dcReg2;

        RelModifier(String parentKey, String dcReg1, String dcReg2) {
            this.parentKey = parentKey;
            this.dcReg1 = dcReg1;
            this.dcReg2 = dcReg2;
        }

        String getName() {
            return this.dcReg1 + "/" + this.dcReg2;
        }

        String getRegId() {
            return this.parentKey + "/" + this.getName();
        }
    }

    static public class AddRelationship extends RelModifier {
        long maxBW;

        public AddRelationship(String parentKey, String dcReg1, String dcReg2, long maxBW) {
            super(parentKey, dcReg1, dcReg2);
            this.maxBW = maxBW;
        }
    }

    static public class ReserveBW extends RelModifier {
        long bw;

        public ReserveBW(String parentKey, String dcReg1, String dcReg2, long bw) {
            super(parentKey, dcReg1, dcReg2);
            this.bw = bw;
        }
    }

    static public class ReturnBW extends RelModifier {
        long bwReturned;

        public ReturnBW(String parentKey, String dcReg1, String dcReg2, long bwReturned) {
            super(parentKey, dcReg1, dcReg2);
            this.bwReturned = bwReturned;
        }
    }

    static public class ReturnBWByRegId {
        String regId;
        long bw;

        public ReturnBWByRegId(String regId, long bw) {
            this.regId = regId;
            this.bw = bw;
        }
    }

    static public class ListRelationships {
    }

    static public class ResetRelationship extends RelModifier {
        public ResetRelationship(String parentKey, String dcReg1, String dcReg2) {
            super(parentKey, dcReg1, dcReg2);
        }
    }
}

