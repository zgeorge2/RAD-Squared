package com.vmware.akka.common;

import com.vmware.common.utils.PrintUtils;
import com.vmware.ignite.common.DModel;
import com.vmware.ignite.common.RegistryManager;
import com.vmware.ignite.util.JobType;
import com.vmware.apps.vap.ignite.JobRegistry;
import com.vmware.common.constants.VapServiceConstants;
import com.vmware.vap.service.dto.JobStatusUpdateDTO;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class BaseVapActorWithTimer extends BaseActorWithTimer {

    protected ConcurrentHashMap<UUID, JobStatusTick> jobTicksForReqMap =
      new ConcurrentHashMap<>();

    protected BaseVapActorWithTimer(RegistryManager rm) {
        super(rm);
    }

    private long maxRetriesForJobCheck = 10;

    protected JobStatusTick initializeJobWithPeriodicCheck(String agentId,
      UUID requestId, JobType jobType, String jobdata,
      long checkForStatusInSecs, List<String> endpoints) {

        HashMap<String, String> endpointStatusMap = new HashMap<>();

        for (String endpoint : endpoints) {
            endpointStatusMap
              .put(endpoint, JobStatusUpdateDTO.JobStatus.STARTING.name());
        }

        String endpointStatusStr = new JSONObject(endpointStatusMap).toString();

        RegistryStateDTO dto = new JobStatusRegistryStateDTO(agentId,
          requestId.toString(),
          jobType,
          JobStatusUpdateDTO.JobStatus.STARTING.name(),
          jobdata,
          System.currentTimeMillis(),
          endpointStatusStr);
        JobRegistry jobReg = reg(JobRegistry.class);
        jobReg.add(dto);

        // Start the timer to check the status of jobs
        Consumer<JobStatusTick> cons = (JobStatusTick jobStatusTick) -> {
            this.checkJobStatus(jobStatusTick);
        };
        JobStatusTick jobStatusTick = new JobStatusTick(TickTypeEnum.PERIODIC,
          cons,
          new CheckJobStatus(requestId, agentId),
          checkForStatusInSecs,
          TimeUnit.SECONDS);
        this.startTimer(jobStatusTick);
        return jobStatusTick;
    }

    protected void updateJob(JobStatusTick jobStatusTick, JobType jobType,
      String jobState) {
        updateJobRegistry(jobStatusTick, jobType, jobState);
        updateTimer(jobStatusTick, jobState);
    }

    protected void updateJobWithEndpointStatus(JobStatusTick jobStatusTick,
      JobType jobType,
      String jobState, Map<String, String> endpointStatusMap) {

        updateJobRegistryWithEndpointStatus(
          jobStatusTick,
          jobType,
          jobState,
          endpointStatusMap);
        updateTimer(jobStatusTick, jobState);
    }

    protected void updateJobRegistryWithEndpointStatus(
      JobStatusTick jobStatusTick, JobType jobType, String jobState,
      Map<String, String> endpointStatusMap) {

        JobRegistry jReg = reg(JobRegistry.class);

        jReg.updateJobEndpointStatus(
          jobStatusTick.getKey().requestID.toString(),
          jobType.getName(),
          endpointStatusMap);

        jReg.updateJobStatus(jobStatusTick.getKey().requestID.toString(),
          jobType.getName(), jobState);

    }

    protected void updateJobRegistry(JobStatusTick jobStatusTick,
      JobType jobType, String jobState) {
        JobRegistry jReg = reg(JobRegistry.class);
        jReg.updateJobStatus(jobStatusTick.getKey().requestID.toString(),
          jobType.getName(),
          JobStatusUpdateDTO.JobStatus.getJobStatus(jobState).name());
    }

    protected void updateTimer(JobStatusTick jobStatusTick, String jobState) {
        if (jobState.equals(state.TaskState.StageEnum.FINISHED.getValue())
          || jobState.equals(state.TaskState.StageEnum.CANCELLED.getValue())
          || jobState.equals(state.TaskState.StageEnum.FAILED.getValue())) {
            this.stopTimer(jobStatusTick);
            PrintUtils.printToActor("Stopping timer......");
        }
    }

    protected abstract void checkJobStatus(JobStatusTick jobStatusTick);

    public static class JobStatusRegistryStateDTO extends RegistryStateDTO {

        public JobStatusRegistryStateDTO(
                    String agentId,
                    String requestId,
                    JobType jobType,
                    String jobStatus,
                    String jobData,
                    long startedAt,
                    String endPointStatus) {
            super(JobRegistry.class, requestId, jobType.getName());
            this.putAttr(VapServiceConstants.ATTR_JOB_ID_KEY, requestId);
            this.putAttr(VapServiceConstants.ATTR_JOB_STATUS_KEY, jobStatus);
            this.putAttr(VapServiceConstants.ATTR_JOB_TYPE_KEY, jobType.getName());
            this.putAttr(VapServiceConstants.ATTR_AGENT_ID_KEY, agentId);
            this.putAttr(VapServiceConstants.ATTR_JOB_REQUEST_KEY, jobData);
            this.putAttr(VapServiceConstants.ATTR_JOB_STARTED_KEY, startedAt);
            this.putAttr(VapServiceConstants.ATTR_JOB_ENDPOINT_STATUS, endPointStatus);
        }

        public JobStatusRegistryStateDTO(JobRegistry.Job model) {
            super(JobRegistry.class, model);
            this.putAttr(VapServiceConstants.ATTR_JOB_STATUS_KEY, model.getJobStatus());
            this.putAttr(VapServiceConstants.ATTR_JOB_TYPE_KEY, model.getJobType());
        }

        public String getJobType() {
            return (String) this.getAttr(VapServiceConstants.ATTR_JOB_TYPE_KEY);
        }

        public String getJobStatus() {
            return (String) this.getAttr(VapServiceConstants.ATTR_JOB_STATUS_KEY);
        }

        public String getJobId() {
            return (String) this.getAttr(VapServiceConstants.ATTR_JOB_ID_KEY);
        }

        public String getAgentId() {
            return (String) this.getAttr(VapServiceConstants.ATTR_AGENT_ID_KEY);
        }

        public String getAgentData() {
            return (String) this.getAttr(VapServiceConstants.ATTR_JOB_REQUEST_KEY);
        }

        public long getStartedAt() {
            return (long) this.getAttr(VapServiceConstants.ATTR_JOB_STARTED_KEY);
        }

        public String getEndpointStatus(){
            return (String) this.getAttr(VapServiceConstants.ATTR_JOB_ENDPOINT_STATUS);
        }

        @Override
        public DModel toModel() {
            return new JobRegistry.Job(this);
        }

    }

    public class CheckJobStatus {
        private UUID requestID;
        private String cloudProxyID;

        public CheckJobStatus(UUID requestID, String cloudProxyID) {
            this.requestID = requestID;
            this.cloudProxyID = cloudProxyID;
        }

        public UUID getRequestID() {
            return requestID;
        }

        public String getCloudProxyID() {
            return cloudProxyID;
        }
    }

    public class JobStatusTick extends Tick<CheckJobStatus> {
        public JobStatusTick(
                    TickTypeEnum type,
                    Consumer<JobStatusTick> consumer,
                    CheckJobStatus checkJobStatus,
                    long maxTicks,
                    TimeUnit timeUnit) {
            super(type, consumer, checkJobStatus, maxTicks, timeUnit);
        }
    }
}
