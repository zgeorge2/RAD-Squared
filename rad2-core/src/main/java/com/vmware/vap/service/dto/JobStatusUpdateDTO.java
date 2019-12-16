package com.vmware.vap.service.dto;

import state.TaskState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The DTO representing Job Level status update
 */
public class JobStatusUpdateDTO {
    private String jobID;
    private JobStatus jobStatus;
    private List<EndpointStatusUpdateDTO> endpointUpdates;

    public JobStatusUpdateDTO(String jobID, JobStatus jobStatus,
                              List<EndpointStatusUpdateDTO> endpointUpdates) {
        super();
        this.jobID = jobID;
        this.jobStatus = jobStatus;
        this.endpointUpdates = endpointUpdates;
    }

    public String getJobID() {
        return jobID;
    }

    public JobStatus getJobStatus() {
        return jobStatus;
    }

    public List<EndpointStatusUpdateDTO> getEndpointUpdates() {
        return endpointUpdates;
    }

    public enum JobStatus {
        STARTING, SUCCESS, INPROGRESS, FAILED;
        static Map<String, JobStatus> jobStatusEnum = new HashMap<>(TaskState.StageEnum.values().length);

        static {
            jobStatusEnum.put(TaskState.StageEnum.CREATED.toString(), JobStatus.STARTING);
            jobStatusEnum.put(TaskState.StageEnum.FINISHED.toString(), JobStatus.SUCCESS);
            jobStatusEnum.put(TaskState.StageEnum.STARTED.toString(), JobStatus.INPROGRESS);
            jobStatusEnum.put(TaskState.StageEnum.FAILED.toString(), JobStatus.FAILED);
            jobStatusEnum.put(TaskState.StageEnum.CANCELLED.toString(), JobStatus.FAILED);
        }

        public static JobStatus getJobStatus(String status) {
            return jobStatusEnum.get(status);
        }
    }
}
