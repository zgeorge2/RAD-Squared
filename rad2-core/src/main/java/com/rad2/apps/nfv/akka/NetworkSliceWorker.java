/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.apps.nfv.akka;

import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.dispatch.BoundedMessageQueueSemantics;
import akka.dispatch.RequiresMessageQueue;
import com.rad2.akka.aspects.ActorMessageHandler;
import com.rad2.akka.common.BaseActor;
import com.rad2.akka.common.RegistryStateDTO;
import com.rad2.akka.router.WorkerActor;
import com.rad2.akka.router.WorkerClassArgs;
import com.rad2.apps.nfv.ctrl.NFVAppController;
import com.rad2.apps.nfv.ignite.FunctionRequestRegistry;
import com.rad2.apps.nfv.ignite.NetRelSliceRegistry;
import com.rad2.apps.nfv.ignite.NetResSliceRegistry;
import com.rad2.apps.nfv.ignite.ThirdPartyRegistry;
import com.rad2.common.utils.PrintUtils;
import com.rad2.ignite.common.RegistryManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class NetworkSliceWorker extends BaseActor implements WorkerActor,
    RequiresMessageQueue<BoundedMessageQueueSemantics> {
    public static final String NETWORK_SLICE_MASTER_ROUTER = "networkSliceMaster";
    public static final String BANNER_KEY = "NETWORK_SLICE_BANNER_KEY";
    private String id; // the id of this routee.
    private String banner; // an arbitrary placeholder arg - used for nothing

    private NetworkSliceWorker(RegistryManager rm, String id, String banner) {
        super(rm);
        this.id = id;
        this.banner = banner;
    }

    static public Props props(WorkerClassArgs args) {
        return Props.create(NetworkSliceWorker.class, args.getRM(), args.getId(),
            args.getArg(BANNER_KEY));
    }

    private ThirdPartyRegistry getTPReg() {
        return reg(ThirdPartyRegistry.class);
    }

    private FunctionRequestRegistry getFRReg() {
        return reg(FunctionRequestRegistry.class);
    }

    private NetResSliceRegistry getResSlcReg() {
        return reg(NetResSliceRegistry.class);
    }

    private NetRelSliceRegistry getRelSlcReg() {
        return reg(NetRelSliceRegistry.class);
    }

    private ActorSelection getTPRouter() {
        return getAU().getActor(getAU().getLocalSystemName(), ThirdPartyWorker.THIRD_PARTY_MASTER_ROUTER);
    }

    private ActorSelection getResRouter() {
        return getAU().getActor(getAU().getLocalSystemName(),
            ProviderResourceWorker.PROVIDER_RESOURCE_MASTER_ROUTER);
    }

    private ActorSelection getRelRouter() {
        return getAU().getActor(getAU().getLocalSystemName(),
            ProviderRelationshipWorker.PROVIDER_RELATIONSHIP_MASTER_ROUTER);
    }

    @Override
    public Receive createReceive() {
        return super.createReceive()
            .orElse(receiveBuilder()
                .match(SaveNetResSlice.class, this::saveNetResSlice)
                .match(SaveNetRelSlice.class, this::saveNetRelSlice)
                .match(DeleteNetResSlice.class, this::deleteNetResSlice)
                .match(DeleteNetRelSlice.class, this::deleteNetRelSlice)
                .match(ListSlices.class, this::listSlices)
                .match(BuySlice.class, this::buySlice)
                .match(ReturnSlice.class, this::returnSlice)
                .build());
    }

    @ActorMessageHandler
    private void saveNetResSlice(SaveNetResSlice arg) {
        RegistryStateDTO dto = new NetResSliceRegistry.NetResSliceRegDTO(arg.id,
            arg.name, arg.oper, arg.resRegId, arg.cpu, arg.mem,
            arg.funcReqRegId, arg.vendorRegId, arg.lics);
        String regId = reg(dto).add(dto);
        PrintUtils.printToActor("****** Added Network Resource Slice [%s]: [%s] ******",
            self().path().toString(), dto);
    }

    @ActorMessageHandler
    private void saveNetRelSlice(SaveNetRelSlice arg) {
        RegistryStateDTO dto = new NetRelSliceRegistry.NetRelSliceRegDTO(arg.id,
            arg.name, arg.oper, arg.relRegId, arg.bw);
        String regId = reg(dto).add(dto);
        PrintUtils.printToActor("****** Added Network Relationship Slice [%s]: [%s] ******",
            self().path().toString(), dto);
    }

    @ActorMessageHandler
    private void deleteNetResSlice(DeleteNetResSlice arg) {
        NetResSliceRegistry.D_NFV_NetResSlice model = getResSlcReg().get(arg.sliceRegId);
        getResSlcReg().remove(model);
        PrintUtils.printToActor("****** Deleted Network Resource Slice [%s]: [%s] ******",
            self().path().toString(), model);
    }

    @ActorMessageHandler
    private void deleteNetRelSlice(DeleteNetRelSlice arg) {
        NetRelSliceRegistry.D_NFV_NetRelSlice model = getRelSlcReg().get(arg.sliceRegId);
        getRelSlcReg().remove(model);
        PrintUtils.printToActor("****** Deleted Network Relationship Slice [%s]: [%s] ******",
            self().path().toString(), model);
    }

    @ActorMessageHandler
    private void listSlices(ListSlices arg) {
        StringBuffer sb = new StringBuffer();
        // prep res func reqs of the slice
        sb.append(String.format("\n\t%30s %30s %30s %15s %15s %30s %30s %15s\n", "SliceId/Name", "Operator",
            "Resource", "Cpu", "Mem", "Func Req", "Third Party", "Requested Lics"));
        sb.append(String.format("\t%30s %30s %30s %15s %15s %30s %30s %15s\n",
            String.join("", Collections.nCopies(30, "-")),
            String.join("", Collections.nCopies(30, "-")),
            String.join("", Collections.nCopies(30, "-")),
            String.join("", Collections.nCopies(15, "-")),
            String.join("", Collections.nCopies(15, "-")),
            String.join("", Collections.nCopies(30, "-")),
            String.join("", Collections.nCopies(30, "-")),
            String.join("", Collections.nCopies(15, "-"))));
        Function<NetResSliceRegistry.D_NFV_NetResSlice, Boolean> resFunc = (nrs) -> {
            sb.append(String.format("\t%s\n", nrs));
            return true;
        };
        this.getResSlcReg().applyToAll(resFunc);

        // prep rels of the slice
        sb.append(String.format("\n\t%30s %30s %30s %30s\n", "SliceId/Name", "Operator", "Relationship",
            "Requested BW"));
        sb.append(String.format("\t%30s %30s %30s %30s\n",
            String.join("", Collections.nCopies(30, "-")),
            String.join("", Collections.nCopies(30, "-")),
            String.join("", Collections.nCopies(30, "-")),
            String.join("", Collections.nCopies(30, "-"))));
        Function<NetRelSliceRegistry.D_NFV_NetRelSlice, Boolean> relFunc = (nrs) -> {
            sb.append(String.format("\t%s\n", nrs));
            return true;
        };
        this.getRelSlcReg().applyToAll(relFunc);

        // print out the res func reqs and rels of the slice
        PrintUtils.printToActor("Worker:[%s]", self().path().toString());
        PrintUtils.printToActor("%s", sb.toString());
    }

    @ActorMessageHandler
    private void buySlice(BuySlice arg) {
        // Use licenses out of Func Requests held by the operator for each resFuncRequest
        arg.resFuncReqSlices.forEach(res -> {
            // 0. updated lics used in this Operator's purchased Function Request
            this.getTPRouter().tell(new ThirdPartyWorker.UseLicsFromFunctionRequest(res.oper,
                res.funcReqName, res.lics), self());
            // 1. Get the venName and venFunc from funcReq stored in registry
            FunctionRequestRegistry.D_NFV_FuncReq fr = getFRReg().get(res.getFRRegId());
            // 2. Get the vendor model using the vendor name and function name from the func req
            ThirdPartyRegistry.D_NFV_ThirdPartyModel vm = getTPReg().get(fr.getVRegId());
            // 3. now reserve the needed cpu/mem from the resource
            this.getResRouter().tell(new ProviderResourceWorker.ReserveResource(res.resNameSpace,
                res.resName, vm.getCpuPer(), vm.getMemPer()), self());
            // 4. save the network resource slice for future reference
            self().tell(new SaveNetResSlice(res.id, res.name, res.oper, res.getResRegId(),
                vm.getCpuPer(), vm.getMemPer(), res.getFRRegId(), fr.getVRegId(), res.lics), self());
        });
        PrintUtils.printToActor("****** Buying Net Resource Func Request Slices [%s] ******",
            self().path().toString());

        // Reserve slices for each relationship
        arg.rels.forEach(rel -> {
            // 0. updated bandwidth used by the slice
            this.getRelRouter().tell(rel.rel, self());
            // 1. save the network relationship slice for future reference
            self().tell(new SaveNetRelSlice(rel.id, rel.name, rel.oper, rel.getRelRegId(), rel.getBW()),
                self());
        });
        PrintUtils.printToActor("****** Buying Net Resource Func Request Slices [%s] ******",
            self().path().toString());
    }

    @ActorMessageHandler
    private void returnSlice(ReturnSlice arg) {
        // return resource func req slices
        getResSlcReg().applyToChildrenOfParent(arg.sliceId,
            res -> {
                // return the lics consumed to the FuncReq owned by this oper
                getTPRouter().tell(new ThirdPartyWorker
                    .ReturnLicsToFunctionRequestById(res.getFuncReqRegId(), res.getLics()), self());
                // return the cpu/mem consumed by the resource
                getResRouter().tell(new ProviderResourceWorker.ReturnResourceById(res.getResRegId(),
                    res.getCpu(), res.getMem()), self());
                // remove the resource slice
                self().tell(new DeleteNetResSlice(res.getKey()), self());
                return null;
            });

        // return relationship slices
        getRelSlcReg().applyToChildrenOfParent(arg.sliceId,
            rel -> {
                // return the bandwidth borrowed
                getRelRouter().tell(new ProviderRelationshipWorker.ReturnBWByRegId(rel.getRelRegId(),
                    rel.getBandwidth()), self());
                // remove the network slice itself
                self().tell(new DeleteNetRelSlice(rel.getKey()), self());
                return true;
            });
    }

    /**
     * Classes used for receive method above.
     */
    static public class ListSlices {
    }

    static public class SaveNetResSlice extends BaseSlice {
        String resRegId;
        long cpu;
        long mem;
        String funcReqRegId;
        String vendorRegId;
        long lics;

        public SaveNetResSlice(String sliceId, String sliceName, String oper,
                               String resRegId, long cpu, long mem,
                               String funcReqRegId, String vendorRegId, long lics) {
            super(sliceId, sliceName, oper);
            this.resRegId = resRegId;
            this.cpu = cpu;
            this.mem = mem;
            this.funcReqRegId = funcReqRegId;
            this.vendorRegId = vendorRegId;
            this.lics = lics;
        }
    }

    static public class SaveNetRelSlice extends BaseSlice {
        String relRegId;
        long bw;

        public SaveNetRelSlice(String id, String name, String oper, String relRegId, long bw) {
            super(id, name, oper);
            this.relRegId = relRegId;
            this.bw = bw;
        }
    }

    static public class DeleteNetResSlice {
        String sliceRegId;

        public DeleteNetResSlice(String sliceRegId) {
            this.sliceRegId = sliceRegId;
        }
    }

    static public class DeleteNetRelSlice {
        String sliceRegId;

        public DeleteNetRelSlice(String sliceRegId) {
            this.sliceRegId = sliceRegId;
        }
    }

    static public class BuySlice {
        List<ResFuncReqSlice> resFuncReqSlices;
        List<RelSlice> rels;

        public BuySlice(NFVAppController.NetworkSliceDTO dto) {
            this.rels = new ArrayList<>();
            dto.getRels().forEach(rel -> {
                rels.add(new RelSlice(rel));
            });
            this.resFuncReqSlices = new ArrayList<>();
            dto.getResources().forEach(res -> {
                this.resFuncReqSlices.add(new ResFuncReqSlice(res));
            });
        }
    }

    static public class ReturnSlice {
        String sliceId; // every resFuncReq and rel owned by the slice is reset

        public ReturnSlice(String id) {
            this.sliceId = id;
        }
    }

    static public class BaseSlice {
        String id; // the slice id to which this belongs
        String name; // the name of this particular res/rel  request
        String oper; // the operator requesting this part of the slice

        public BaseSlice(String id, String name, String oper) {
            this.id = id;
            this.name = name;
            this.oper = oper;
        }
    }

    static public class ResFuncReqSlice extends BaseSlice {
        String resNameSpace; // resource name space
        String resName; // resource name
        String funcReqName; // the func req name
        long lics; // num of lics of func to install on the resource

        public ResFuncReqSlice(NFVAppController.ResFuncReqSlice r) {
            super(r.getId(), r.getName(), r.getOper());
            this.resNameSpace = r.getResNS();
            this.resName = r.getResName();
            this.funcReqName = r.getFuncName();
            this.lics = r.getLics();
        }

        String getResRegId() {
            return this.resNameSpace + "/" + this.resName;
        }

        String getFRRegId() {
            return this.oper + "/" + this.funcReqName;
        }
    }

    public static class RelSlice extends BaseSlice {
        ProviderRelationshipWorker.ReserveBW rel;

        public RelSlice(NFVAppController.RelSlice r) {
            super(r.getId(), r.getName(), r.getOper());
            this.rel = new ProviderRelationshipWorker.ReserveBW(r.getNamespace(), r.getDC1(), r.getDC2(),
                r.getBandwidth());
        }

        String getRelRegId() {
            return rel.getRegId();
        }

        long getBW() {
            return rel.bw;
        }
    }
}

