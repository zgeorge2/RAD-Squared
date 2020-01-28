package com.rad2.apps.nfv.akka;

import akka.actor.Props;
import akka.dispatch.BoundedMessageQueueSemantics;
import akka.dispatch.RequiresMessageQueue;
import com.rad2.akka.aspects.ActorMessageHandler;
import com.rad2.akka.common.BaseActor;
import com.rad2.akka.common.RegistryStateDTO;
import com.rad2.akka.router.WorkerActor;
import com.rad2.akka.router.WorkerClassArgs;
import com.rad2.apps.nfv.ignite.FunctionRequestRegistry;
import com.rad2.apps.nfv.ignite.ThirdPartyRegistry;
import com.rad2.common.utils.PrintUtils;
import com.rad2.ignite.common.RegistryManager;

import java.util.Collections;
import java.util.function.Function;

public class ThirdPartyWorker extends BaseActor implements WorkerActor,
    RequiresMessageQueue<BoundedMessageQueueSemantics> {
    public static final String THIRD_PARTY_MASTER_ROUTER = "thirdPartyMasterRouter";
    public static final String BANNER_KEY = "THIRD_PARTY_BANNER_KEY";
    private String id; // the id of this routee.
    private String banner; // an arbitrary placeholder arg - used for nothing

    private ThirdPartyWorker(RegistryManager rm, String id, String banner) {
        super(rm);
        this.id = id;
        this.banner = banner;
    }

    static public Props props(WorkerClassArgs args) {
        return Props.create(ThirdPartyWorker.class, args.getRM(), args.getId(),
            args.getArg(BANNER_KEY));
    }

    private ThirdPartyRegistry getTPReg() {
        return reg(ThirdPartyRegistry.class);
    }

    private FunctionRequestRegistry getFRReg() {
        return reg(FunctionRequestRegistry.class);
    }

    @Override
    public Receive createReceive() {
        return super.createReceive()
            .orElse(receiveBuilder()
                .match(AddVendor.class, this::addVendor)
                .match(ListVendors.class, this::listVendors)
                .match(BuyLicenses.class, this::buyLicenses)
                .match(ReturnLicenses.class, this::returnLicenses)
                .match(ReturnAllLicenses.class, this::returnAllLicenses)
                .match(BuyFunctionRequest.class, this::buyFunctionRequest)
                .match(ListFunctionRequests.class, this::listFunctionRequests)
                .match(UseLicsFromFunctionRequest.class, this::useLicsFromFunctionRequest)
                .match(ReturnLicsToFunctionRequest.class, this::returnLicsToFunctionRequest)
                .match(ReturnLicsToFunctionRequestById.class, this::returnLicsToFunctionRequestById)
                .match(ReturnAllLicsToFunctionRequest.class, this::returnAllLicsToFunctionRequest)
                .build());
    }

    @ActorMessageHandler
    private void addVendor(AddVendor arg) {
        RegistryStateDTO dto = new ThirdPartyRegistry.ThirdPartyRegDTO(arg.name, arg.function, arg.cpuPer,
            arg.memPer, arg.maxLicenses);
        String regId = reg(dto).add(dto);
        PrintUtils.printToActor("****** Created Vendor [%s]: [%s] ******", self().path().toString(), dto);
    }

    @ActorMessageHandler
    private void listVendors(ListVendors arg) {
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("\n\t%30s %15s %15s %15s %15s\n", "Vendor/Func", "CPU Per Lic",
            "Mem Per Lic", "Max Lics", "Avail Lics"));
        sb.append(String.format("\t%30s %15s %15s %15s %15s\n",
            String.join("", Collections.nCopies(30, "-")),
            String.join("", Collections.nCopies(15, "-")),
            String.join("", Collections.nCopies(15, "-")),
            String.join("", Collections.nCopies(15, "-")),
            String.join("", Collections.nCopies(15, "-"))));
        Function<ThirdPartyRegistry.D_NFV_ThirdPartyModel, Boolean> func = (m) -> {
            sb.append(String.format("\t%s\n", m));
            return true;
        };
        this.getTPReg().applyToAll(func);
        PrintUtils.printToActor("Worker:[%s]", self().path().toString());
        PrintUtils.printToActor("%s", sb.toString());
    }

    @ActorMessageHandler
    private void buyLicenses(BuyLicenses arg) {
        ThirdPartyRegistry.D_NFV_ThirdPartyModel model = getTPReg().buyLicenses(arg.getRegId(), arg.lics);
        PrintUtils.printToActor("****** Buy licenses w/ Worker [%s] for Vendor/Function: [%s] ******",
            self().path().toString(), model);
    }

    @ActorMessageHandler
    private void returnLicenses(ReturnLicenses arg) {
        ThirdPartyRegistry.D_NFV_ThirdPartyModel model = getTPReg().returnLicenses(arg.getRegId(), arg.lics);
        PrintUtils.printToActor("****** Returned licenses w/ Worker [%s] for Vendor/Function: [%s] ******",
            self().path().toString(), model);
    }

    @ActorMessageHandler
    private void returnAllLicenses(ReturnAllLicenses arg) {
        ThirdPartyRegistry.D_NFV_ThirdPartyModel model = getTPReg().returnAllLicenses(arg.getRegId());
        PrintUtils.printToActor("****** Returned All licenses w/ Worker [%s] of Vendor/Function: [%s] " +
                "******",
            self().path().toString(), model);
    }

    @ActorMessageHandler
    private void listFunctionRequests(ListFunctionRequests arg) {
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("\n\t%30s %30s %30s %15s  %15s\n", "Oper/FRName", "V Name",
            "V Func", "Bought Lics", "Avail Lics"));
        sb.append(String.format("\t%30s %30s %30s %15s  %15s\n",
            String.join("", Collections.nCopies(30, "-")),
            String.join("", Collections.nCopies(30, "-")),
            String.join("", Collections.nCopies(30, "-")),
            String.join("", Collections.nCopies(15, "-")),
            String.join("", Collections.nCopies(15, "-"))));
        Function<FunctionRequestRegistry.D_NFV_FuncReq, Boolean> func = (fr) -> {
            sb.append(String.format("\t%s\n", fr));
            return true;
        };
        this.getFRReg().applyToAll(func);
        PrintUtils.printToActor("Worker:[%s]", self().path().toString());
        PrintUtils.printToActor("%s", sb.toString());
    }

    @ActorMessageHandler
    private void buyFunctionRequest(BuyFunctionRequest arg) {
        // Purchase the function requests against the operator
        RegistryStateDTO dto = new FunctionRequestRegistry.FuncReqRegDTO(arg.oper, arg.frName,
            arg.vn, arg.vf, arg.lics);
        String regId = reg(dto).add(dto);
        PrintUtils.printToActor("****** Registered Function Request [%s]: [%s] ******",
            self().path().toString(), dto);
        // now buy the requested licenses from the TP Vendor by sending message to self
        self().tell(new BuyLicenses(arg.vn, arg.vf, arg.lics), self());
    }

    @ActorMessageHandler
    private void useLicsFromFunctionRequest(UseLicsFromFunctionRequest arg) {
        FunctionRequestRegistry.D_NFV_FuncReq model = getFRReg().useLicenses(arg.getRegId(), arg.lics);
        PrintUtils.printToActor("****** Use licenses [%d] w/ Worker [%s] from Function Request: [%s] ******",
            arg.lics, self().path().toString(), model);
    }

    @ActorMessageHandler
    private void returnLicsToFunctionRequest(ReturnLicsToFunctionRequest arg) {
        FunctionRequestRegistry.D_NFV_FuncReq model = getFRReg().returnLicenses(arg.getRegId(), arg.lics);
        PrintUtils.printToActor("****** Ret licenses [%d] w/ Worker [%s] to Function Request: [%s] ******",
            arg.lics, self().path().toString(), model);
    }

    @ActorMessageHandler
    private void returnLicsToFunctionRequestById(ReturnLicsToFunctionRequestById arg) {
        FunctionRequestRegistry.D_NFV_FuncReq model = getFRReg().returnLicenses(arg.key, arg.lics);
        PrintUtils.printToActor("****** Ret licenses [%d] w/ Worker [%s] to Function Request: [%s] ******",
            arg.lics, self().path().toString(), model);
    }

    @ActorMessageHandler
    private void returnAllLicsToFunctionRequest(ReturnAllLicsToFunctionRequest arg) {
        FunctionRequestRegistry.D_NFV_FuncReq model = getFRReg().returnAllLicenses(arg.getRegId());
        PrintUtils.printToActor("****** Ret All licenses w/ Worker [%s] to Function Request: [%s] ******",
            self().path().toString(), model);
    }

    /**
     * Classes used for receive method above.
     */
    static public class AddVendor extends BaseVendor {
        long cpuPer; // num CPU's needed per license to run this function
        long memPer; // amount of Mem needed per license to run this function
        long maxLicenses; // total num of licenses of the function offered by this vendor
        long availLicenses; // leftover licenses available to hand out

        public AddVendor(String name, String function, long cpuPer, long memPer, long maxLicenses) {
            super(name, function);
            this.cpuPer = cpuPer;
            this.memPer = memPer;
            this.maxLicenses = maxLicenses;
            this.availLicenses = this.maxLicenses;
        }
    }

    static class BaseVendor {
        String name; // the name of the vendor acts as parentKey
        String function; // the name of the function offered by the vendor

        BaseVendor(String name, String function) {
            this.name = name;
            this.function = function;
        }

        String getRegId() {
            return this.name + "/" + this.function;
        }
    }

    static public class BuyLicenses extends BaseVendor {
        long lics;

        public BuyLicenses(String name, String function, long lics) {
            super(name, function);
            this.lics = lics;
        }
    }

    static public class ReturnLicenses extends BaseVendor {
        long lics;

        public ReturnLicenses(String name, String function, long lics) {
            super(name, function);
            this.lics = lics;
        }
    }

    static public class ListVendors {
    }

    static public class ReturnAllLicenses extends BaseVendor {
        public ReturnAllLicenses(String name, String function) {
            super(name, function);
        }
    }

    static public class BaseFunctionRequest {
        String oper;
        String frName;

        public BaseFunctionRequest(String oper, String frName) {
            this.oper = oper;
            this.frName = frName;
        }

        String getRegId() {
            return this.oper + "/" + this.frName;
        }
    }

    static public class BaseLicsFunctionRequest extends BaseFunctionRequest {
        long lics;

        public BaseLicsFunctionRequest(String oper, String frName, long lics) {
            super(oper, frName);
            this.lics = lics;
        }
    }

    static public class BuyFunctionRequest extends BaseLicsFunctionRequest {
        String vn;
        String vf;

        public BuyFunctionRequest(String oper, String frName, String vn, String vf, long lics) {
            super(oper, frName, lics);
            this.vn = vn;
            this.vf = vf;
        }
    }

    static public class UseLicsFromFunctionRequest extends BaseLicsFunctionRequest {
        public UseLicsFromFunctionRequest(String oper, String frName, long lics) {
            super(oper, frName, lics);
        }
    }

    static public class ReturnLicsToFunctionRequest extends BaseLicsFunctionRequest {
        public ReturnLicsToFunctionRequest(String oper, String frName, long lics) {
            super(oper, frName, lics);
        }
    }

    static public class ReturnLicsToFunctionRequestById {
        String key;
        long lics;

        public ReturnLicsToFunctionRequestById(String key, long lics) {
            this.key = key;
            this.lics = lics;
        }
    }

    static public class ListFunctionRequests {
    }

    static public class ReturnAllLicsToFunctionRequest extends BaseFunctionRequest {
        public ReturnAllLicsToFunctionRequest(String oper, String frName) {
            super(oper, frName);
        }
    }
}

