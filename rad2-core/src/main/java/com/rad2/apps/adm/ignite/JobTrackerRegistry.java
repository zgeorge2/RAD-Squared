/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.apps.adm.ignite;

import com.rad2.akka.common.RegistryStateDTO;
import com.rad2.apps.adm.akka.JobTrackerWorker;
import com.rad2.common.utils.PrintUtils;
import com.rad2.ctrl.deps.IJobRef;
import com.rad2.ignite.common.BaseModelRegistry;
import com.rad2.ignite.common.DModel;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

public class JobTrackerRegistry extends BaseModelRegistry<JobTrackerRegistry.DJobStateModel> {
    @Override
    public Class getModelClass() {
        return DJobStateModel.class;
    }

    public DJobStateModel failedJob(String key, String result) {
        return this.apply(key, jt -> jt.failedJob(result));
    }

    public DJobStateModel updateJob(String key, JobStatusEnum jobStatus, String result) {
        return this.apply(key, jt -> jt.updateJob(jobStatus, result));
    }

    /**
     * Removes successful, but stale Registry entry corresponding to key.
     */
    public boolean removeJob(String key) {
        boolean ret = false;
        DJobStateModel jobState = this.get(key);
        // remove the job only if it has completed its task successfully
        if (jobState != null && jobState.isSuccessful()) {
            this.remove(jobState);
            ret = true;
        } else {
            PrintUtils.printToActor("REMOVE JOB: Failed to find Model for key[%s]", key);
        }
        return ret;
    }

    public static class DJobStateModel extends DModel implements IJobRef {
        @QuerySqlField
        private String jobStatus;
        @QuerySqlField
        private String result;
        @QuerySqlField
        private long lastUpdateTimestamp;

        public DJobStateModel(JobTrackerWorker.JobTrackerRegStateDTO dto) {
            super(dto);
            this.jobStatus = dto.getJobStatus().name();
            this.result = dto.getResult();
            this.lastUpdateTimestamp = dto.getLastUpdate();
        }

        public DJobStateModel updateJob(JobStatusEnum jobStatus, String result) {
            this.updateJob(jobStatus); // first update status
            if (isSuccessful()) this.result += result; // update result ONLY when successful
            return this;
        }

        public DJobStateModel updateJob(JobStatusEnum jobStatus) {
            this.jobStatus = jobStatus.name();
            this.lastUpdateTimestamp = System.currentTimeMillis();
            return this;
        }

        public DJobStateModel failedJob(String result) {
            this.jobStatus = JobStatusEnum.JOB_STATUS_FAILED.name();
            this.result += result;
            this.lastUpdateTimestamp = System.currentTimeMillis();
            return this;
        }

        @Override
        public RegistryStateDTO toRegistryStateDTO() {
            return new JobTrackerWorker.JobTrackerRegStateDTO(this);
        }

        @Override
        public Class getActorClass() {
            return JobTrackerWorker.class;
        }

        /**
         * Only reincarnate Actors whose JobStatus is "In Progress" or "Not Started"
         *
         * @return
         */
        @Override
        public boolean shouldReincarnateActor() {
            return ((this.getJobStatus().compareTo(JobStatusEnum.JOB_STATUS_IN_PROGRESS) == 0) ||
                    (this.getJobStatus().compareTo(JobStatusEnum.JOB_STATUS_NOT_STARTED) == 0));
        }

        public long getLastUpdate() {
            return lastUpdateTimestamp;
        }

        public JobStatusEnum getJobStatus() {
            return JobStatusEnum.get(this.jobStatus);
        }

        public String getResult() {
            return result;
        }

        /**
         * @param age the time period to check against in millis
         * @return true if this model's lastUpdateTimestamp is older than age
         */
        public boolean isStale(long age) {
            return Math.abs(System.currentTimeMillis() - lastUpdateTimestamp) > age;
        }

        public boolean isSuccessful() {
            return JobStatusEnum.JOB_STATUS_SUCCESS.compareTo(getJobStatus()) == 0;
        }

        public boolean isFailed() {
            return JobStatusEnum.JOB_STATUS_FAILED.compareTo(getJobStatus()) == 0;
        }

        public boolean isResultRetrievalOnly() {
            return JobStatusEnum.JOB_STATUS_RETRIEVAL_ONLY.compareTo(getJobStatus()) == 0;
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(super.toString());
            sb.append(String.format("\t[STATUS:%s][%s]\n", this.getJobStatus(), this.getResult()));
            return sb.toString();
        }
    }
}
