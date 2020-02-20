package com.rad2.apps.adm.akka;

import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.dispatch.BoundedMessageQueueSemantics;
import akka.dispatch.RequiresMessageQueue;
import com.rad2.akka.common.BaseActor;
import com.rad2.akka.common.BasicDeferredMessage;
import com.rad2.akka.common.IDeferred;
import com.rad2.akka.common.RegistryStateDTO;
import com.rad2.akka.router.WorkerActor;
import com.rad2.akka.router.WorkerClassArgs;
import com.rad2.apps.adm.ignite.JobStatusEnum;
import com.rad2.apps.adm.ignite.JobTrackerRegistry;
import com.rad2.ctrl.deps.IJobRef;
import com.rad2.ctrl.deps.JobRef;
import com.rad2.ignite.common.DModel;
import com.rad2.ignite.common.RegistryManager;

import java.util.function.Consumer;

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

    private JobConsumers getJCons() {
        return JobConsumers.getInstance();
    }

    private JobTrackerRegistry getJTReg() {
        return reg(JobTrackerRegistry.class);
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
                        .match(InitJobRetrieval.class, this::initJobRetrieval)
                        .match(FailedGetResult.class, this::failedGetResult) // FailedGetResult first
                        .match(FailedJob.class, this::failedJob) // FailedJob after FailedGetResult - inheritance
                        .match(UpdateJob.class, this::updateJob)
                        .match(CleanUp.class, this::cleanUp)
                        .match(GetResult.class, this::getResult)
                        .match(NotifyConsumers.class, this::notifyConsumers)
                        .match(RemoveConsumers.class, this::removeConsumers)
                        .build());
    }

    private void initJob(InitJob arg) {
        // store the job persistently if it doesn't already have an entry, else only add the consumer
        initJobWithStatus(arg, JobStatusEnum.JOB_STATUS_NOT_STARTED);
    }

    private void initJobRetrieval(InitJobRetrieval arg) {
        // if the job is already there, then ignore and add consumer.
        // If it isn't there - then this is an attempt to get a result for a non-existent job.
        // Add the consumer only
        initJobWithStatus(arg, JobStatusEnum.JOB_STATUS_RETRIEVAL_ONLY);
    }

    private void initJobWithStatus(JobReqWithConsumer arg, JobStatusEnum status) {
        // add the consumer
        getJCons().addConsumer(new AddConsumer(arg.jobRef(), arg.cons));
        // persist the job
        JobTrackerRegistry.DJobStateModel model = getJTReg().get(arg.jobRegId());
        if (model == null) {
            // initialize the job in the registry
            String jobResultIntro = String.format(JobStatusEnum.JOB_RESULT_INTRO_FORMAT, arg.jobRegId(), arg.jobRegId());
            RegistryStateDTO dto = new JobTrackerRegStateDTO(arg.jobRef().getParentKey(), arg.jobRef().getName(),
                    status, jobResultIntro);
            reg(dto).add(dto);
        }
        // AFTER the consumer has been added and the registry updated. Then perform the nextStep
        arg.nextStep.accept(arg);
    }

    /**
     * This method can be called incrementally to update the result in the Registry.
     * Each time it is called - it also notifies the consumers registered against this IJobRef.
     */
    private void updateJob(UpdateJob arg) {
        // store the result persistently
        JobTrackerRegistry.DJobStateModel model = getJTReg().updateJob(arg.regId(), arg.jobStatus, arg.result);
        // allow consumers of the jobref to get the updated result
        sendNotifyConsumers(arg, model.getResult(), false);
    }

    /**
     * This method prepares a final result based on whatever is currently available
     * for the result in the registry and  notifies the consumers registered against this IJobRef.
     */
    private void getResult(GetResult arg) {
        // Get the IJobRef regid and prepare the result
        String origJobRegId = arg.jobRegId();
        JobTrackerRegistry.DJobStateModel model = getJTReg().get(origJobRegId);
        if (model != null) {
            String ret = "";
            if (model.isSuccessful() || model.isFailed()) {
                ret = model.getResult();
                sendNotifyConsumers(arg.jobRef(), ret, true);
            } else if (model.isResultRetrievalOnly()) {
                // this job was created for result retrieval and has no result. return a failure
                self().tell(new FailedGetResult(arg.jobRef()), self()); // failed job will send the notify
            } else {
                ret = model.getJobStatus().toString();
                sendNotifyConsumers(arg.jobRef(), ret, true);
            }
        }
    }

    private void failedJob(FailedJob arg) {
        JobTrackerRegistry.DJobStateModel model = getJTReg().failedJob(arg.regId(), arg.result);
        // allow consumers of the jobref to get the updated result
        sendNotifyConsumers(arg, model.getResult(), false);
    }

    private void failedGetResult(FailedGetResult arg) {
        JobTrackerRegistry.DJobStateModel model = getJTReg().failedJob(arg.regId(), arg.result);
        // allow consumers of the jobref to get the updated result
        sendNotifyConsumers(arg, model.getResult(), true);
    }

    // send the result to the node that has the consumers
    // for this jobref. If local is set to true - then use the local JR only.
    private void sendNotifyConsumers(IJobRef ijr, String result, boolean local) {
        ActorSelection jr = local ? getJR() : getJR(ijr);
        jr.tell(new NotifyConsumers(ijr, result), self());
    }

    // locally update the consumers corresponding to the job
    private void notifyConsumers(NotifyConsumers arg) {
        // allow consumers of the jobref to get the updated result
        getJCons().notifyConsumers(arg);
    }

    private void cleanUp(CleanUp arg) {
        // clean up the registry of older entries
        getJTReg().removeChildrenOfParentMatching(arg.getParentKey(), k -> (k.isStale(arg.getAge())));
        // also clean up the corresponding consumers
        getJTReg().applyToChildrenOfParent(arg.getParentKey(),
                k -> {
                    if (k.isStale(arg.getAge())) {
                        sendRemoveConsumers(k);
                    }
                    return true;
                });
    }

    // send the removeConsumers to the node that has the consumers
    // for this jobref.
    private void sendRemoveConsumers(IJobRef ijr) {
        getJR(ijr).tell(new RemoveConsumers(ijr), self());
    }

    // locally remove consumers corresponding to the job
    private void removeConsumers(RemoveConsumers arg) {
        // allow consumers of the jobref to get the updated result
        getJCons().removeConsumers(arg);
    }

    /**
     * Each class below represents a statement that can be received by this Actor. Note that these messages
     * are immutable structures. Message handling is done in the "createReceive" method of this Actor class.
     */
    public static class JobTrackerRegStateDTO extends RegistryStateDTO {
        public static final String ATTR_JOB_STATUS_KEY = "JOB_STATUS_KEY";
        public static final String ATTR_JOB_RESULT_KEY = "JOB_RESULT_KEY";
        public static final String ATTR_JOB_LAST_UPDATE_KEY = "JOB_LAST_UPDATE_KEY";

        public JobTrackerRegStateDTO(String parentKey, String name, JobStatusEnum jobStatus, String result) {
            super(JobTrackerRegistry.class, parentKey, name);
            this.putAttr(ATTR_JOB_STATUS_KEY, jobStatus);
            this.putAttr(ATTR_JOB_RESULT_KEY, result);
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

    /**
     * Messages for this Actor
     */
    public static class JobReqWithConsumer extends BasicDeferredMessage<String> {
        Consumer<String> cons; // the consumer of the result
        Consumer<IDeferred<String>> nextStep; // the next step to perform with this request

        public JobReqWithConsumer(IDeferred<String> req,
                                  Consumer<String> cons, Consumer<IDeferred<String>> nextStep) {
            super(req);
            this.cons = cons;
            this.nextStep = r -> {
                // note that r itself is ignored. Its the original req that needs to be used in the nextStep
                nextStep.accept(req); // chain the local nextStep to the argument nextStep in order to capture req
            };
        }
    }

    public static class InitJob extends JobReqWithConsumer {

        public InitJob(IDeferred<String> req,
                       Consumer<String> cons, Consumer<IDeferred<String>> nextStep) {
            super(req, cons, nextStep);
        }
    }

    public static class InitJobRetrieval extends JobReqWithConsumer {

        public InitJobRetrieval(IDeferred<String> req,
                                Consumer<String> cons, Consumer<IDeferred<String>> nextStep) {
            super(req, cons, nextStep);
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
    }

    public static class FailedJob extends JobRef {
        public String result;

        public FailedJob(IJobRef ijr) {
            super(ijr);
            this.result = JobStatusEnum.JOB_FAILED_FORMAT;
        }
    }

    public static class FailedGetResult extends FailedJob {
        public FailedGetResult(IJobRef ijr) {
            super(ijr);
        }
    }

    public static class GetResult extends BasicDeferredMessage<String> {
        public GetResult(IDeferred<String> req) {
            super(req);
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


    public static class AddConsumer extends JobRef {
        Consumer<String> cons;

        public AddConsumer(IJobRef ijr, Consumer<String> cons) {
            super(ijr);
            this.cons = cons;
        }
    }

    public static class NotifyConsumers extends JobRef {
        String result;

        public NotifyConsumers(IJobRef ijr, String result) {
            super(ijr);
            this.result = result;
        }

        public NotifyConsumers() {
            super();
            this.result = null;
        }
    }

    public static class RemoveConsumers extends JobRef {
        public RemoveConsumers(IJobRef ijr) {
            super(ijr);
        }
    }
}
