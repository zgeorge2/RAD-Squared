package com.vmware.apps.vap.ignite;

import com.vmware.akka.common.BaseVapActorWithTimer;
import com.vmware.akka.common.RegistryStateDTO;
import com.vmware.common.constants.VapServiceConstants;
import com.vmware.ignite.common.BaseModelRegistry;
import com.vmware.ignite.common.DModel;
import org.apache.ignite.cache.query.annotations.QuerySqlField;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * The Ignite registry for maintaining JOB state
 */
public class JobRegistry extends BaseModelRegistry<JobRegistry.Job> {

    Logger logger = LoggerFactory.getLogger(JobRegistry.class);

    @Override
    protected Class getModelClass() {
        return Job.class;
    }

    public void updateJobStatus(String parentKey, String id, String jobStatus) {
        String key = this.getKey(parentKey, id);
        this.apply(key, job -> job.jobStatus = jobStatus);
    }

    public void updateJobStatus(String parentKey, String id, String jobStatus, String jobStatusText) {
        String key = this.getKey(parentKey, id);
        this.apply(key, job -> {
            job.jobStatus = jobStatus;
            job.jobStatusText = jobStatusText;
            return job;
        });
    }

    public void updateJobEndpointStatus(String parentKey, String id,
      Map<String, String> endpointStatusMap) {
        String key = this.getKey(parentKey, id);
        Job job = this.get(key);
        try {
            HashMap<String, String> endpointMapFromReg = getEndpointStatusMap(
              job.endpointStatus);

            endpointStatusMap.forEach((endpointId, value) -> {
                if (!endpointMapFromReg.get(endpointId)
                  .equalsIgnoreCase(VapServiceConstants.SUCCESS) && !endpointMapFromReg
                  .get(endpointId).equalsIgnoreCase(VapServiceConstants.FAILED)) {
                    endpointMapFromReg.put(endpointId, value);
                }
            });

            job.endpointStatus = new JSONObject(endpointMapFromReg).toString();

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        this
          .apply(key, jobInReg -> jobInReg.endpointStatus = job.endpointStatus);
    }

    public HashMap<String, String> getEndpointStatusMap(String endpointStatus)
      throws IOException {
        return new ObjectMapper()
                  .readValue(
                    endpointStatus,
                    new TypeReference<HashMap<String, String>>() {
                    });
    }

    public String getJobKey(String parentKey, String name) {
        return this.getKey(parentKey, name);
    }

    public static class Job extends DModel {
        public static final String JOB_TYPE_INSTALL = "Install";
        public static final String JOB_TYPE_UNINSTALL = "Uninstall";
        public static final String JOB_TYPE_START = "Start";
        public static final String JOB_TYPE_STOP = "Stop";
        public static final String JOB_TYPE_UPGRADE = "Upgrade";
        public static final String JOB_TYPE_VALIDATE_CLOUD_ACCOUNT = "Validate_Cloud_Account";
        public static final String JOB_STATUS_STARTING = "STARTING";
        public static final String JOB_STATUS_INPORGRESS = "INPROGRESS";
        public static final String JOB_STATUS_SUCCESS = "SUCCESS";
        public static final String JOB_STATUS_FAILED = "FAILED";
        @QuerySqlField
        private String agentId;
        @QuerySqlField
        private String jobId;
        @QuerySqlField
        private String jobType;
        @QuerySqlField
        private String jobStatus;
        @QuerySqlField
        private String jobStatusText;
        @QuerySqlField
        private long startedAt;
        @QuerySqlField
        private String jobData;
        @QuerySqlField
        private String endpointStatus;

        public Job(BaseVapActorWithTimer.JobStatusRegistryStateDTO dto) {
            super(dto);
            this.jobId = dto.getJobId();
            this.jobType = dto.getJobType();
            this.jobStatus = dto.getJobStatus();
            this.agentId = dto.getAgentId();
            this.jobData = dto.getAgentData();
            this.startedAt = dto.getStartedAt();
            this.endpointStatus = dto.getEndpointStatus();
        }

        @Override
        public RegistryStateDTO toRegistryStateDTO() {
            return new BaseVapActorWithTimer.JobStatusRegistryStateDTO(this);
        }

        public String getJobID() {
            return super.getName();
        }

        public String getJobType() {
            return this.jobType;
        }

        public String getJobStatus() {
            return jobStatus;
        }

        public String getEndpointStatus() { return endpointStatus; }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(super.toString());
            sb.append(String.format("\t[TYPE:%s] [STATUS:%s]\n", this.getJobType(), this.getJobStatus()));
            return sb.toString();
        }
    }
}
