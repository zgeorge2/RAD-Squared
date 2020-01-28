package com.rad2.apps.nfv.akka;

import akka.actor.Props;
import akka.dispatch.BoundedMessageQueueSemantics;
import akka.dispatch.RequiresMessageQueue;
import com.rad2.akka.aspects.ActorMessageHandler;
import com.rad2.akka.common.BaseActor;
import com.rad2.akka.common.RegistryStateDTO;
import com.rad2.akka.router.WorkerActor;
import com.rad2.akka.router.WorkerClassArgs;
import com.rad2.apps.nfv.ignite.ResRegistry;
import com.rad2.common.utils.PrintUtils;
import com.rad2.ignite.common.RegistryManager;

import java.util.Collections;
import java.util.function.Function;

public class ProviderResourceWorker extends BaseActor implements WorkerActor,
    RequiresMessageQueue<BoundedMessageQueueSemantics> {
    public static final String PROVIDER_RESOURCE_MASTER_ROUTER = "provResourceMasterRouter";
    public static final String BANNER_KEY = "PROV_RESOURCE_BANNER_KEY";
    private String id; // the id of this routee.
    private String banner; // an arbitrary placeholder arg - used for nothing

    private ProviderResourceWorker(RegistryManager rm, String id, String banner) {
        super(rm);
        this.id = id;
        this.banner = banner;
    }

    static public Props props(WorkerClassArgs args) {
        return Props.create(ProviderResourceWorker.class, args.getRM(), args.getId(),
            args.getArg(BANNER_KEY));
    }

    private ResRegistry getDCReg() {
        return reg(ResRegistry.class);
    }

    @Override
    public Receive createReceive() {
        return super.createReceive()
            .orElse(receiveBuilder()
                .match(AddDatacenter.class, this::addDatacenter)
                .match(ListDatacenters.class, this::listDatacenters)
                .match(InitDatacenter.class, this::initDatacenter)
                .match(DeleteDatacenter.class, this::deleteDatacenter)
                .match(ReserveResource.class, this::reserveCPU)
                .match(ReturnResource.class, this::returnCPU)
                .match(ReturnResourceById.class, this::returnCPUById)
                .match(ResetDCsForNamespace.class, this::resetDatacenter)
                .build());
    }

    @ActorMessageHandler
    private void addDatacenter(AddDatacenter arg) {
        RegistryStateDTO dto = new ResRegistry.ResRegDTO(arg.parentKey, arg.name, arg.numCPU,
            arg.maxMem);
        String regId = reg(dto).add(dto);
        PrintUtils.printToActor("****** Created Host [%s]: [%s] ******", self().path().toString(), dto);
    }

    @ActorMessageHandler
    private void listDatacenters(ListDatacenters arg) {
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("\n\t%30s %15s %15s %15s %15s\n", "NS/Datacenter Name", "Num CPU", "Avail " +
            "CPU", "Max Mem", "Avail Mem"));
        sb.append(String.format("\t%30s %15s %15s %15s %15s\n",
            String.join("", Collections.nCopies(30, "-")),
            String.join("", Collections.nCopies(15, "-")),
            String.join("", Collections.nCopies(15, "-")),
            String.join("", Collections.nCopies(15, "-")),
            String.join("", Collections.nCopies(15, "-"))));
        Function<ResRegistry.D_NFV_ResModel, Boolean> func = (m) -> {
            sb.append(String.format("\t%s\n", m));
            return true;
        };
        this.getDCReg().applyToAll(func);
        PrintUtils.printToActor("Worker:[%s]", self().path().toString());
        PrintUtils.printToActor("%s", sb.toString());
    }

    @ActorMessageHandler
    private void initDatacenter(InitDatacenter arg) {
        ResRegistry.D_NFV_ResModel model = this.getDCReg().get(arg.getRegId());
    }

    @ActorMessageHandler
    private void deleteDatacenter(DeleteDatacenter arg) {
        ResRegistry.D_NFV_ResModel model = this.getDCReg().get(arg.getRegId());
    }

    @ActorMessageHandler
    private void reserveCPU(ReserveResource arg) {
        String regId = arg.getRegId();
        ResRegistry.D_NFV_ResModel model = getDCReg().reserveResource(regId, arg.cpuNeeded,
            arg.memNeeded);
        PrintUtils.printToActor("****** Reserved CPU/Mem using Worker [%s] for Datacenter: [%s] ******",
            self().path().toString(), model);
    }

    @ActorMessageHandler
    private void returnCPU(ReturnResource arg) {
        String regId = arg.getRegId();
        ResRegistry.D_NFV_ResModel model = getDCReg().returnResource(regId, arg.cpuReturned,
            arg.memReturned);
        PrintUtils.printToActor("****** Returned CPU/Mem using Worker [%s] for Datacenter: [%s] ******",
            self().path().toString(), model);
    }

    @ActorMessageHandler
    private void returnCPUById(ReturnResourceById arg) {
        ResRegistry.D_NFV_ResModel model = getDCReg().returnResource(arg.resKey, arg.cpuReturned,
            arg.memReturned);
        PrintUtils.printToActor("****** Returned CPU/Mem using Worker [%s] for Datacenter: [%s] ******",
            self().path().toString(), model);
    }

    @ActorMessageHandler
    private void resetDatacenter(ResetDCsForNamespace arg) {
        this.getDCReg().resetResource(arg.ns);
    }

    /**
     * Classes used for receive method above.
     */
    static public class AddDatacenter {
        String parentKey; // think of this as the namespace to which this datacenter belongs
        String name; // a unique name of this Datacenter within the namespace
        long numCPU;
        long maxMem;

        public AddDatacenter(String parentKey, String name, long numCPU, long maxMem) {
            this.parentKey = parentKey;
            this.name = name;
            this.numCPU = numCPU;
            this.maxMem = maxMem;
        }
    }

    static public class DCModifier {
        String parentKey;
        String name;

        public DCModifier(String parentKey, String name) {
            this.parentKey = parentKey;
            this.name = name;
        }

        String getRegId() {
            return this.parentKey + "/" + this.name;
        }
    }

    static public class InitDatacenter extends DCModifier {
        public InitDatacenter(String parentKey, String name) {
            super(parentKey, name);
        }
    }

    static public class DeleteDatacenter extends DCModifier {
        public DeleteDatacenter(String parentKey, String name) {
            super(parentKey, name);
        }
    }

    static public class ReserveResource extends DCModifier {
        long cpuNeeded;
        long memNeeded;

        public ReserveResource(String parentKey, String name, long cpuNeeded, long memNeeded) {
            super(parentKey, name);
            this.cpuNeeded = cpuNeeded;
            this.memNeeded = memNeeded;
        }
    }

    static public class ReturnResource extends DCModifier {
        long cpuReturned;
        long memReturned;

        public ReturnResource(String parentKey, String name, long cpuReturned, long memReturned) {
            super(parentKey, name);
            this.cpuReturned = cpuReturned;
            this.memReturned = memReturned;
        }
    }

    static public class ReturnResourceById {
        String resKey;
        long cpuReturned;
        long memReturned;

        public ReturnResourceById(String key, long cpuReturned, long memReturned) {
            this.resKey = key;
            this.cpuReturned = cpuReturned;
            this.memReturned = memReturned;
        }
    }

    static public class ListDatacenters {
    }

    static public class ResetDCsForNamespace {
        String ns;

        public ResetDCsForNamespace(String ns) {
            this.ns = ns;
        }
    }
}

