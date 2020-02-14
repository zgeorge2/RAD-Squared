package com.rad2.apps.adm.akka;

import akka.actor.Props;
import akka.dispatch.BoundedMessageQueueSemantics;
import akka.dispatch.RequiresMessageQueue;
import com.rad2.akka.common.BaseActor;
import com.rad2.akka.common.IDeferredMessage;
import com.rad2.akka.common.IDeferredRequest;
import com.rad2.akka.common.RegistryStateDTO;
import com.rad2.akka.router.WorkerActor;
import com.rad2.akka.router.WorkerClassArgs;
import com.rad2.apps.adm.ignite.JobStatusEnum;
import com.rad2.apps.adm.ignite.JobTrackerRegistry;
import com.rad2.ctrl.deps.IJobRef;
import com.rad2.ctrl.deps.JobRef;
import com.rad2.ignite.common.DModel;
import com.rad2.ignite.common.RegistryManager;

/**
 * JobTrackerWorker Actors work with JobTrackerRouters to provide JobTracking service per node in the cluster
 */
public class JobTrackerWorker extends BaseActor implements WorkerActor,
        RequiresMessageQueue<BoundedMessageQueueSemantics> {
    public static final String JOB_TRACKER_MASTER_ROUTER = "jtRtr";
    public static final String BANNER_KEY = "JOB_TRACKER_BANNER_KEY";
    private String id; // the id of this routee.
    private String banner;

    private JobTrackerWorker(RegistryManager rm, String id, String banner) {
        super(rm);
        this.id = id;
        this.banner = banner;
    }

    static public Props props(WorkerClassArgs args) {
        return Props.create(JobTrackerWorker.class, args.getRM(), args.getId(),
                args.getArg(BANNER_KEY));
    }

    @Override
    public Receive createReceive() {
        return super.createReceive()
                .orElse(receiveBuilder()
                        .match(InitJob.class, this::initJob)
                        .match(InProgressJob.class, this::inProgressJob)
                        .match(FailedJob.class, this::failedJob)
                        .match(UpdateJob.class, this::updateJob)
                        .match(RemoveJob.class, this::removeJob)
                        .match(CleanUp.class, this::cleanUp)
                        .match(GetJobResult.class, this::getJobResult)
                        .build());
    }

    private void initJob(InitJob arg) {
        RegistryStateDTO dto = new JobTrackerRegStateDTO(arg.getParentKey(), arg.getName(),
                JobStatusEnum.JOB_STATUS_NOT_STARTED);
        reg(dto).add(dto);
    }

    private void failedJob(FailedJob arg) {
        this.getJTReg().failedJob(arg.regId());
    }

    private void inProgressJob(InProgressJob arg) {
        this.getJTReg().inProgressJob(arg.regId());
    }

    private void updateJob(UpdateJob arg) {
        this.getJTReg().updateJob(arg.regId(), arg.jobStatus, arg.result);
    }

    private void getJobResult(GetJobResult arg) {
        String ret = "Failed to get JobResult. May have expired or is invalid!";
        JobTrackerRegistry.DJobStateModel model = this.getJTReg().get(arg.getJobRefRegId());
        if (model != null) {
            if (model.isSuccessful()) {
                ret = model.getResult();
            } else {
                ret = model.getJobStatus().toString();
            }
        }
        arg.setResponse(ret);
    }

    private void removeJob(RemoveJob arg) {
        boolean ret = this.getJTReg().removeJob(arg.regId());
    }

    private void cleanUp(CleanUp arg) {
        this.getJTReg().cleanupEntriesOlderThan(arg.getParentKey(), arg.getAge());
    }

    private JobTrackerRegistry getJTReg() {
        return reg(JobTrackerRegistry.class);
    }

    /**
     * Each class below represents a statement that can be received by this Actor. Note that these messages
     * are immutable structures. Message handling is done in the "createReceive" method of this Actor class.
     */
    public static class JobTrackerRegStateDTO extends RegistryStateDTO {
        public static final String ATTR_JOB_STATUS_KEY = "JOB_STATUS_KEY";
        public static final String ATTR_JOB_RESULT_KEY = "JOB_RESULT_KEY";
        public static final String ATTR_JOB_LAST_UPDATE_KEY = "JOB_LAST_UPDATE_KEY";

        public JobTrackerRegStateDTO(String parentKey, String name, JobStatusEnum jobStatus) {
            super(JobTrackerRegistry.class, parentKey, name);
            this.putAttr(ATTR_JOB_STATUS_KEY, jobStatus);
            this.putAttr(ATTR_JOB_RESULT_KEY, "");
            this.putAttr(ATTR_JOB_LAST_UPDATE_KEY, System.currentTimeMillis());
        }

        public JobTrackerRegStateDTO(JobTrackerRegistry.DJobStateModel model) {
            super(JobTrackerRegistry.class, model);
            this.putAttr(ATTR_JOB_STATUS_KEY, model.getJobStatus());
            this.putAttr(ATTR_JOB_RESULT_KEY, model.getResult());
            this.putAttr(ATTR_JOB_LAST_UPDATE_KEY, model.getLastUpdate());
        }

        @Override
        public DModel toModel() {
            return new JobTrackerRegistry.DJobStateModel(this);
        }

        public JobStatusEnum getJobStatus() {
            return (JobStatusEnum) this.getAttr(ATTR_JOB_STATUS_KEY);
        }

        public String getResult() {
            return (String) this.getAttr(ATTR_JOB_RESULT_KEY);
        }

        public long getLastUpdate() {
            return (long) this.getAttr(ATTR_JOB_LAST_UPDATE_KEY);
        }
    }

    public static class InitJob extends JobRef {
        public InitJob(IJobRef ijr) {
            super(ijr);
        }
    }

    public static class FailedJob extends JobRef {
        public FailedJob(IJobRef ijr) {
            super(ijr);
        }
    }

    public static class InProgressJob extends JobRef {
        public InProgressJob(IJobRef ijr) {
            super(ijr);
        }
    }

    public static class UpdateJob extends JobRef {
        public JobStatusEnum jobStatus;
        public String result;

        public UpdateJob(IJobRef ijr, JobStatusEnum jobStatus, String result) {
            super(ijr);
            this.jobStatus = jobStatus;
            this.result = result;
        }

        public UpdateJob(IJobRef ijr, JobStatusEnum jobStatus) {
            this(ijr, jobStatus, "");
        }
    }

    static public class GetJobResult implements IDeferredMessage<String> {
        IDeferredRequest<String> req;

        public GetJobResult(IDeferredRequest<String> req) {
            this.req = req;
        }

        @Override
        public IDeferredRequest<String> getEmbeddedRequest() {
            return req;
        }
    }

    public static class RemoveJob extends JobRef {
        public RemoveJob(IJobRef ijr) {
            super(ijr);
        }
    }

    public static class CleanUp {
        private String parentKey;
        private long age;

        public CleanUp(String parentKey, long age) {
            this.parentKey = parentKey;
            this.age = age;
        }

        public String getParentKey() {
            return parentKey;
        }

        public long getAge() {
            return age;
        }
    }
}
