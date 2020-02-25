/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.apps.nfv.ctrl;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rad2.apps.nfv.akka.NetworkSliceWorker;
import com.rad2.apps.nfv.akka.ProviderRelationshipWorker;
import com.rad2.apps.nfv.akka.ProviderResourceWorker;
import com.rad2.common.utils.PrintUtils;
import com.rad2.ctrl.BaseController;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class NFVAppController extends BaseController {
    public void createOrder(CreateOrderDTO dto) {
    }

    /**
     * Network Slices related controls
     */
    public void buySlices(BuyNetworkSlicesDTO dto) {
        ActorSelection ir = getNetSliceRouter();
        PrintUtils.printToActor("Buying Network Slices: %s", dto);
        dto.getSlices().forEach(sl -> {
            ir.tell(new NetworkSliceWorker.BuySlice(sl), ActorRef.noSender());
        });
    }

    public void retSlices(ReturnNetworkSlicesDTO dto) {
        ActorSelection ir = getNetSliceRouter();
        PrintUtils.printToActor("Returning Network Slices: %s", dto);
        dto.getSlices().forEach(sl -> {
            ir.tell(new NetworkSliceWorker.ReturnSlice(sl), ActorRef.noSender());
        });
    }

    public void listSlices() {
        ActorSelection ir = getNetSliceRouter();
        PrintUtils.printToActor("Listing Network Slices ...");
        ir.tell(new NetworkSliceWorker.ListSlices(), ActorRef.noSender());
    }

    /**
     * Resources related controls
     */
    /**
     *
     */
    public void addResources(ResourcesDTO dto) {
        ActorSelection ir = getResRouter();
        PrintUtils.printToActor("Adding Resources: %s", dto);
        dto.getResources().forEach(dc -> {
            ir.tell(new ProviderResourceWorker.AddDatacenter(dc.getNamespace(), dc.getName(), dc.cpu, dc.mem),
                ActorRef.noSender());
        });
    }

    public void listResources() {
        ActorSelection ir = getResRouter();
        PrintUtils.printToActor("Listing Resources ...");
        ir.tell(new ProviderResourceWorker.ListDatacenters(), ActorRef.noSender());
    }

    public void resetAllResources(ResetAllResourcesDTO dto) {
        ActorSelection ir = getResRouter();
        dto.getNamespaces().forEach(ns -> {
            PrintUtils.printToActor("Reset All Resources for Namespace [%s]...", ns);
            ir.tell(new ProviderResourceWorker.ResetDCsForNamespace(ns), ActorRef.noSender());
        });
    }

    public void reserveResourceSlices(ReserveResourceSlicesDTO dto) {
        ActorSelection ir = getResRouter();
        PrintUtils.printToActor("Reserving Resource Slice: %s", dto);
        dto.getResources().forEach(dc -> {
            ir.tell(new ProviderResourceWorker.ReserveResource(dc.getNamespace(), dc.getName(), dc.cpu,
                dc.mem), ActorRef.noSender());
        });
    }

    public void returnResourceSlices(ReturnResourceSlicesDTO dto) {
        ActorSelection ir = getResRouter();
        PrintUtils.printToActor("Returning Resource Slice: %s", dto);
        dto.getResources().forEach(dc -> {
            ir.tell(new ProviderResourceWorker.ReturnResource(dc.getNamespace(), dc.getName(), dc.cpu,
                dc.mem), ActorRef.noSender());
        });
    }

    /**
     * Relationships related controls
     */
    /**
     *
     */
    public void addRelationships(RelsDTO dto) {
        ActorSelection ir = getRelRouter();
        PrintUtils.printToActor("Adding Relationships: %s", dto);
        dto.getRels().forEach(rel -> {
            ir.tell(new ProviderRelationshipWorker.AddRelationship(rel.getNamespace(), rel.getDC1(),
                rel.getDC2(), rel.getBandwidth()), ActorRef.noSender());
        });
    }

    public void listRelationships() {
        ActorSelection ir = getRelRouter();
        PrintUtils.printToActor("Listing Relationships ...");
        ir.tell(new ProviderRelationshipWorker.ListRelationships(), ActorRef.noSender());
    }

    public void resetAllRelationships(ResetAllRelsDTO dto) {
        ActorSelection ir = getRelRouter();
        dto.getRels().forEach(rel -> {
            PrintUtils.printToActor("Reset Relationship: [%s]", rel.getName());
            ir.tell(new ProviderRelationshipWorker.ResetRelationship(rel.getNamespace(), rel.getDC1(),
                    rel.getDC2()),
                ActorRef.noSender());
        });
    }

    public void reserveRelationshipSlices(ReserveRelSlicesDTO dto) {
        ActorSelection ir = getRelRouter();
        PrintUtils.printToActor("Reserving Relationship Slice: %s", dto);
        dto.getRels().forEach(rel -> {
            ir.tell(new ProviderRelationshipWorker.ReserveBW(rel.getNamespace(), rel.getDC1(), rel.getDC2(),
                rel.getBandwidth()), ActorRef.noSender());
        });
    }

    public void returnRelationshipSlices(ReturnRelSlicesDTO dto) {
        ActorSelection ir = getRelRouter();
        PrintUtils.printToActor("Returning Relationship Slice: %s", dto);
        dto.getRels().forEach(rel -> {
            ir.tell(new ProviderRelationshipWorker.ReturnBW(rel.getNamespace(), rel.getDC1(), rel.getDC2(),
                rel.getBandwidth()), ActorRef.noSender());
        });
    }

    @Override
    public List<Class> getDependenciesList() {
        List<Class> ret = new ArrayList<>();
        ret.add(NFVAppInitializer.class);
        return ret;
    }

    private ActorSelection getResRouter() {
        return getAU().getActor(getAU().getLocalSystemName(),
            ProviderResourceWorker.PROVIDER_RESOURCE_MASTER_ROUTER);
    }

    private ActorSelection getRelRouter() {
        return getAU().getActor(getAU().getLocalSystemName(),
            ProviderRelationshipWorker.PROVIDER_RELATIONSHIP_MASTER_ROUTER);
    }

    private ActorSelection getNetSliceRouter() {
        return getAU().getActor(getAU().getLocalSystemName(), NetworkSliceWorker.NETWORK_SLICE_MASTER_ROUTER);
    }

    /**
     * DTO classes for controller methods
     */
    public static class CreateOrderDTO {
        private String name; // must be unique across all TDs

        @JsonProperty
        public String getName() {
            return name;
        }
    }

    public static class ReturnNetworkSlicesDTO {
        private List<String> slices;

        @JsonProperty
        public List<String> getSlices() {
            return slices;
        }
    }

    public static class BuyNetworkSlicesDTO extends NetworkSlicesDTO {
    }

    public static class NetworkSlicesDTO {
        private List<NetworkSliceDTO> slices;

        public NetworkSlicesDTO() {
            this.slices = new ArrayList<>();
        }

        public List<NetworkSliceDTO> getSlices() {
            return slices;
        }
    }

    public static class NetworkSliceDTO {
        private List<ResFuncReqSlice> resources; // the reservation of resources (nodes) in the slice
        private List<RelSlice> rels; // the reservation of BW from relationships in the slice

        @JsonProperty
        public List<ResFuncReqSlice> getResources() {
            return resources;
        }

        @JsonProperty
        public List<RelSlice> getRels() {
            return rels;
        }
    }

    public static class BaseSlice {
        private String id; // the slice id to which this DCFuncReq belongs
        private String name; // the name of this particular DCFuncReq
        private String oper; // the operator requesting this slice and DCFuncReq

        @JsonProperty
        public String getId() {
            return id;
        }

        @JsonProperty
        public String getName() {
            return name;
        }

        @JsonProperty
        public String getOper() {
            return oper;
        }
    }

    public static class ResFuncReqSlice extends BaseSlice {
        private BaseDatacenterDTO res; // the DC on which the func req is to be installed
        private LicsOfFuncReq funcReq; // the func req

        @JsonProperty
        public BaseDatacenterDTO getRes() {
            return res;
        }

        //
        public String getResNS() {
            return getRes().getNamespace();
        }

        //
        public String getResName() {
            return getRes().getName();
        }

        @JsonProperty
        public LicsOfFuncReq getFuncReq() {
            return funcReq;
        }

        //
        public String getFuncName() {
            return getFuncReq().getName();
        }

        //
        public long getLics() {
            return getFuncReq().getLics();
        }
    }

    public static class LicsOfFuncReq {
        private String name;
        private long lics;

        @JsonProperty
        public String getName() {
            return name;
        }

        @JsonProperty
        public long getLics() {
            return lics;
        }
    }

    public static class RelSlice extends BaseSlice {
        private RelDTOSpec rel;

        @JsonProperty
        public RelDTOSpec getRel() {
            return rel;
        }

        // NOT a JsonProperty  - since it is defaulted
        public String getNamespace() {
            return getRel().getNamespace();
        }

        // NOT a JsonProperty - only an accessor method
        public String getDC1() {
            return getRel().getDC1();
        }

        // NOT a JsonProperty - only an accessor method
        public String getDC2() {
            return getRel().getDC2();
        }

        // NOT a JsonProperty - only an accessor method
        public long getBandwidth() {
            return getRel().getBandwidth();
        }
    }

    /**
     * RESOURCE SLICES DTOs
     */
    public static class ResetAllResourcesDTO {
        private List<String> namespaces;

        public ResetAllResourcesDTO() {
            this.namespaces = new ArrayList<>();
        }

        @JsonProperty
        public List<String> getNamespaces() {
            return namespaces;
        }
    }

    public static class ReturnResourceSlicesDTO extends ResourcesDTO {
    }

    public static class ReserveResourceSlicesDTO extends ResourcesDTO {
    }

    public static class ResourcesDTO {
        private List<DatacenterDTO> resources;

        public ResourcesDTO() {
            this.resources = new ArrayList<>();
        }

        @JsonProperty
        public List<DatacenterDTO> getResources() {
            return resources;
        }
    }

    public static class BaseDatacenterDTO {
        private String namespace;
        private String name;

        @JsonProperty
        public String getName() {
            return name;
        }

        @JsonProperty
        public String getNamespace() {
            return namespace;
        }
    }

    public static class DatacenterDTO extends BaseDatacenterDTO {
        private long cpu; // used to set, reserve, reset
        private long mem; // used to set, reserve, reset

        @JsonProperty
        public long getCpu() {
            return cpu;
        }

        @JsonProperty
        public long getMem() {
            return mem;
        }
    }

    /**
     * RELATIONSHIP SLICES DTOs
     */
    public static class ResetAllRelsDTO {
        private List<RelDTO> rels;

        public ResetAllRelsDTO() {
            this.rels = new ArrayList<>();
        }

        @JsonProperty
        public List<RelDTO> getRels() {
            return rels;
        }
    }

    public static class ReturnRelSlicesDTO extends RelsDTO {
    }

    public static class ReserveRelSlicesDTO extends RelsDTO {
    }

    public static class RelsDTO {
        private List<RelDTOSpec> rels;

        public RelsDTO() {
            this.rels = new ArrayList<>();
        }

        @JsonProperty
        public List<RelDTOSpec> getRels() {
            return rels;
        }
    }

    public static class RelDTOSpec extends RelDTO {
        private long bandwidth; // used to set, reserve, return

        @JsonProperty
        public long getBandwidth() {
            return bandwidth;
        }
    }

    public static class RelDTO {
        private String dc1; // regId of DC 1
        private String dc2; // regId of DC 2

        // NOT a JsonProperty - only an accessor method
        public String getName() {
            return dc1 + "/" + dc2;
        }

        // NOT a JsonProperty  - since it is defaulted
        public String getNamespace() {
            return "DN";
        }

        @JsonProperty
        public String getDC1() {
            return dc1;
        }

        @JsonProperty
        public String getDC2() {
            return dc2;
        }
    }
}
