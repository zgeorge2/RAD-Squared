/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.apps.nfv.ctrl;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rad2.apps.nfv.akka.ThirdPartyWorker;
import com.rad2.common.utils.PrintUtils;
import com.rad2.ctrl.BaseController;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ThirdPartyController extends BaseController {
    public void addVendors(AddVendorsDTO dto) {
        ActorSelection ir = getThirdPartyRouter();
        PrintUtils.print("Adding Vendors: %s", dto);
        dto.getVendors().forEach(v -> {
            ir.tell(new ThirdPartyWorker.AddVendor(v.getName(), v.getFunction(), v.cpu, v.mem, v.getLics()),
                ActorRef.noSender());
        });
    }

    public void listVendors() {
        ActorSelection ir = getThirdPartyRouter();
        PrintUtils.print("Listing Vendors ...");
        ir.tell(new ThirdPartyWorker.ListVendors(), ActorRef.noSender());
    }

    public void returnAllLicenses(ReturnAllLicensesDTO dto) {
        ActorSelection ir = getThirdPartyRouter();
        dto.getVendors().forEach(v -> {
            PrintUtils.print("Return All Licenses for [%s]...", v);
            ir.tell(new ThirdPartyWorker.ReturnAllLicenses(v.getName(), v.getFunction()),
                ActorRef.noSender());
        });
    }

    public void buyLicenses(BuyLicensesDTO dto) {
        ActorSelection ir = getThirdPartyRouter();
        PrintUtils.print("Buying Licenses: %s", dto);
        dto.getVendors().forEach(v -> {
            ir.tell(new ThirdPartyWorker.BuyLicenses(v.getName(), v.getFunction(), v.getLics()),
                ActorRef.noSender());
        });
    }

    public void returnLicenses(ReturnLicensesDTO dto) {
        ActorSelection ir = getThirdPartyRouter();
        PrintUtils.print("Returning Licenses: %s", dto);
        dto.getVendors().forEach(v -> {
            ir.tell(new ThirdPartyWorker.ReturnLicenses(v.getName(), v.getFunction(), v.getLics()),
                ActorRef.noSender());
        });
    }

    public void listFunctionRequests() {
        ActorSelection ir = getThirdPartyRouter();
        PrintUtils.print("Listing Function Requests ...");
        ir.tell(new ThirdPartyWorker.ListFunctionRequests(), ActorRef.noSender());
    }

    public void buyFunctionRequests(BuyFunctionRequestsDTO dto) {
        ActorSelection ir = getThirdPartyRouter();
        PrintUtils.print("Buying Function Requests : %s", dto);
        dto.getFuncReqs().forEach(fr -> {
            ir.tell(new ThirdPartyWorker.BuyFunctionRequest(fr.getOper(),fr.getName(),
                    fr.getV().getName(), fr.getV().getFunction(), fr.getV().getLics()),
                ActorRef.noSender());
        });
    }

    @Override
    public List<Class> getDependenciesList() {
        List<Class> ret = new ArrayList<>();
        ret.add(ThirdPartyInitializer.class);
        return ret;
    }

    private ActorSelection getThirdPartyRouter() {
        return getAU().getActor(getAU().getLocalSystemName(), ThirdPartyWorker.THIRD_PARTY_MASTER_ROUTER);
    }

    /**
     * DTO classes for controller methods
     */
    public static class ReturnAllLicensesDTO extends VendorsDTO<BaseVendorDTO> {
    }

    public static class ReturnLicensesDTO extends VendorsDTO<VendorLicsDTO> {
    }

    public static class BuyLicensesDTO extends VendorsDTO<VendorLicsDTO> {
    }

    public static class AddVendorsDTO extends VendorsDTO<VendorDetailsDTO> {
    }

    public static class VendorsDTO<K extends BaseVendorDTO> {
        private List<K> vendors;

        public VendorsDTO() {
            vendors = new ArrayList<>();
        }

        @JsonProperty
        public List<K> getVendors() {
            return vendors;
        }
    }

    public static class BaseVendorDTO {
        private String name;
        private String function;

        @JsonProperty
        public String getName() {
            return name;
        }

        @JsonProperty
        public String getFunction() {
            return function;
        }
    }

    public static class VendorLicsDTO extends BaseVendorDTO {
        private long lics; // used to buy, return, reset

        @JsonProperty
        public long getLics() {
            return lics;
        }
    }

    public static class VendorDetailsDTO extends VendorLicsDTO {
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

    public static class BuyFunctionRequestsDTO {
        private List<FunctionReqDTO> funcReqs;

        public BuyFunctionRequestsDTO() {
            funcReqs = new ArrayList<>();
        }

        @JsonProperty
        public List<FunctionReqDTO> getFuncReqs() {
            return funcReqs;
        }
    }

    public static class FunctionReqDTO {
        private String oper;
        private String name;
        private VendorLicsDTO v;

        @JsonProperty
        public String getOper() {
            return oper;
        }

        @JsonProperty
        public String getName() {
            return name;
        }

        @JsonProperty
        public VendorLicsDTO getV() {
            return v;
        }
    }
}
