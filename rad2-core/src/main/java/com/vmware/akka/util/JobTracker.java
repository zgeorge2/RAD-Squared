package com.vmware.akka.util;

import akka.actor.Props;
import com.vmware.akka.common.BaseActorWithRegState;
import com.vmware.akka.common.RegistryStateDTO;
import com.vmware.ignite.common.DModel;
import com.vmware.ignite.common.RegistryManager;
import com.vmware.ignite.util.JobStatusEnum;
import com.vmware.ignite.util.JobTrackerRegistry;

/**
 * JobTracker Actors are always instantiated at the root of the ActorSystem (under users, though). However,
 * they can be used by any other Actor. Hence, the user field in the Actor isn't necessarily the parent of the
 * JobTracker Actor in the Actor hierarchy
 */
public class JobTracker extends BaseActorWithRegState {
    private JobTracker(RegistryManager rm, RegistryStateDTO dto) {
        super(rm, dto);
    }

    static public Props props(RegistryManager rm, String name, String jobOwner, String jobType) {
        //always instantiated at the root of the ActorSystem
        String systemName = rm.getAU().getLocalSystemName();
        RegistryStateDTO dto = new JobTrackerRegStateDTO(systemName, name, jobOwner, jobType,
            JobStatusEnum.JOB_STATUS_NOT_STARTED,
            JobStatusEnum.JOB_STATUS_NOT_STARTED.name());
        return Props.create(JobTracker.class, rm, dto);
    }

    @Override
    public Receive createReceive() {
        return super.createReceive()
            .orElse(receiveBuilder()
                .match(UpdateJob.class, this::updateJob)
                .build());
    }

    private void updateJob(UpdateJob dto) {
        JobTrackerRegistry.DJobStateModel jobState = this.getJTReg().updateJob(this.getRegId(),
            dto.jobStatus, dto.message);
        // terminate the actor if it has completed its task successfully
        if (jobState != null && (JobStatusEnum.JOB_STATUS_SUCCESS.compareTo(jobState.getJobStatus()) == 0)) {
            self().tell(new Terminate(), self());
            this.getJTReg().remove(jobState);
        }
    }

    private JobTrackerRegistry getJTReg() {
        return reg(JobTrackerRegistry.class);
    }

    /**
     * Each class below represents a statement that can be received by this Actor. Note that these messages
     * are immutable structures. Message handling is done in the "createReceive" method of this Actor class.
     */
    public static class JobTrackerRegStateDTO extends RegistryStateDTO {
        public static final String ATTR_JOB_OWNER_KEY = "JOB_OWNER_KEY";
        public static final String ATTR_JOB_TYPE_KEY = "JOB_TYPE_KEY";
        public static final String ATTR_JOB_STATUS_KEY = "JOB_STATUS_KEY";
        public static final String ATTR_JOB_STATUS_MESSAGE_KEY = "JOB_STATUS_MESSAGE_KEY";

        public JobTrackerRegStateDTO(String parentKey, String name, String jobOwner, String jobType,
                                     JobStatusEnum jobStatus,
                                     String message) {
            super(JobTrackerRegistry.class, parentKey, name);
            this.putAttr(ATTR_JOB_OWNER_KEY, jobOwner);
            this.putAttr(ATTR_JOB_TYPE_KEY, jobType);
            this.putAttr(ATTR_JOB_STATUS_KEY, jobStatus);
            this.putAttr(ATTR_JOB_STATUS_MESSAGE_KEY, message);
        }

        public JobTrackerRegStateDTO(JobTrackerRegistry.DJobStateModel model) {
            super(JobTrackerRegistry.class, model);
            this.putAttr(ATTR_JOB_STATUS_KEY, model.getJobStatus());
            this.putAttr(ATTR_JOB_TYPE_KEY, model.getJobType());
            this.putAttr(ATTR_JOB_STATUS_MESSAGE_KEY, model.getMessage());
        }

        @Override
        public DModel toModel() {
            return new JobTrackerRegistry.DJobStateModel(this);
        }

        public String getJobOwner() {
            return (String) this.getAttr(ATTR_JOB_OWNER_KEY);
        }

        public JobStatusEnum getJobStatus() {
            return (JobStatusEnum) this.getAttr(ATTR_JOB_STATUS_KEY);
        }

        public String getJobType() {
            return (String) this.getAttr(ATTR_JOB_TYPE_KEY);
        }

        public String getMessage() {
            return (String) this.getAttr(ATTR_JOB_STATUS_MESSAGE_KEY);
        }
    }

    public static class UpdateJob {
        public JobStatusEnum jobStatus;
        public String message;

        public UpdateJob(JobStatusEnum jobStatus, String message) {
            this.jobStatus = jobStatus;
            this.message = message;
        }

        public UpdateJob(JobStatusEnum jobStatus) {
            this(jobStatus, jobStatus.name());
        }
    }
}
