package com.rad2.akka.common;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import com.rad2.apps.adm.akka.JobTrackerWorker;
import com.rad2.apps.adm.ignite.JobStatusEnum;
import com.rad2.ctrl.deps.IJobRef;

/**
 * Simple wrapper around calls to JobTrackerWorker via its MasterRouter
 */
public interface IJobWorkerClient {
    ActorSelection getJR();

    ActorRef self();

    default void initJob(IDeferredMessage<String> arg) {
        initJob(arg.getJobRef());
    }

    default void updateJobFailed(IDeferredMessage<String> arg) {
        updateJobFailed(arg.getJobRef());
    }

    default void updateJobSuccess(IDeferredMessage<String> arg, String result) {
        arg.setResponse(result);
        updateJobSuccess(arg.getJobRef(), result);
    }

    default void inProgressJob(IDeferredMessage<String> arg) {
        inProgressJob(arg.getJobRef());
    }

    default void initJob(IJobRef arg) {
        getJR().tell(new JobTrackerWorker.InitJob(arg), self());
    }

    default void updateJobFailed(IJobRef arg) {
        getJR().tell(new JobTrackerWorker.FailedJob(arg), self());
    }

    default void updateJobSuccess(IJobRef arg, String result) {
        getJR().tell(new JobTrackerWorker.UpdateJob(arg, JobStatusEnum.JOB_STATUS_SUCCESS, result), self());
    }

    default void inProgressJob(IJobRef arg) {
        getJR().tell(new JobTrackerWorker.InProgressJob(arg), self());
    }
}
