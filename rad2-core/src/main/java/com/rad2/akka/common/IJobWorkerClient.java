/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.akka.common;

import akka.actor.ActorSelection;
import com.rad2.apps.adm.akka.JobTrackerWorker;
import com.rad2.apps.adm.ignite.JobStatusEnum;
import com.rad2.ctrl.deps.IJobRef;

/**
 * Simple wrapper around calls to JobTrackerWorker via its MasterRouter
 */
public interface IJobWorkerClient extends ExtendedActorBehavior {
    default ActorSelection getJR() {
        return getAU().getActor(getAU().getLocalSystemName(), JobTrackerWorker.JOB_TRACKER_MASTER_ROUTER);
    }

    default ActorSelection getJR(IJobRef ijr) {
        return getAU().getActor(ijr.getParentKey(), JobTrackerWorker.JOB_TRACKER_MASTER_ROUTER);
    }

    default void updateJobFailed(IDeferred<String> arg) {
        updateJobFailed(arg.jobRef());
    }

    default void updateJobSuccess(IDeferred<String> arg, String result) {
        updateJobSuccess(arg.jobRef(), result);
    }

    default void updateJobFailed(IJobRef ijr) {
        getJR().tell(new JobTrackerWorker.FailedJob(ijr), self());
    }

    default void updateJobSuccess(IJobRef ijr, String result) {
        getJR().tell(new JobTrackerWorker.UpdateJob(ijr, JobStatusEnum.JOB_STATUS_SUCCESS, result), self());
    }
}
