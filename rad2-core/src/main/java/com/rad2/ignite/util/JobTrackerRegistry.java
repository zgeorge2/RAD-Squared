package com.rad2.ignite.util;

import com.rad2.akka.common.RegistryStateDTO;
import com.rad2.akka.util.JobTracker;
import com.rad2.ignite.common.BaseModelRegistry;
import com.rad2.ignite.common.DModel;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

public class JobTrackerRegistry extends BaseModelRegistry<JobTrackerRegistry.DJobStateModel> {
    @Override
    public Class getModelClass() {
        return DJobStateModel.class;
    }

    public DJobStateModel updateJob(String key, JobStatusEnum jobStatus, String message) {
        DJobStateModel ret = this.apply(key, jt -> jt.updateJobState(jobStatus, message));
        return ret;
    }

    public static class DJobStateModel extends DModel {
        @QuerySqlField
        private String jobOwner;
        @QuerySqlField
        private String jobType;
        @QuerySqlField
        private String jobStatus;
        @QuerySqlField
        private String message;

        public DJobStateModel(JobTracker.JobTrackerRegStateDTO dto) {
            super(dto);
            this.jobOwner = dto.getJobOwner();
            this.jobType = dto.getJobType();
            this.jobStatus = dto.getJobStatus().name();
            this.message = dto.getMessage();
        }

        public DJobStateModel updateJobState(JobStatusEnum jobStatus, String message) {
            this.jobStatus = jobStatus.name();
            this.message = message;
            return this;
        }

        @Override
        public RegistryStateDTO toRegistryStateDTO() {
            return new JobTracker.JobTrackerRegStateDTO(this);
        }

        @Override
        public Class getActorClass() {
            return JobTracker.class;
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

        public String getJobOwner() {
            return this.jobOwner;
        }

        public String getJobType() {
            return this.jobType;
        }

        public JobStatusEnum getJobStatus() {
            return JobStatusEnum.get(this.jobStatus);
        }

        public String getMessage() {
            return message;
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(super.toString());
            sb.append(String.format("\t[OWNER:%s]\t[TYPE:%s]\t[STATUS:%s][%s]\n", this.getJobOwner(),
                this.getJobType(), this.getJobStatus(), this.getMessage()));
            return sb.toString();
        }
    }
}
