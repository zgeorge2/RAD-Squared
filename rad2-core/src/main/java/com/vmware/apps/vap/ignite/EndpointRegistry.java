package com.vmware.apps.vap.ignite;

import com.vmware.akka.common.RegistryStateDTO;
import com.vmware.apps.vap.akka.BootstrapActor;
import com.vmware.common.constants.VapServiceConstants;
import com.vmware.common.utils.PrintUtils;
import com.vmware.ignite.common.BaseModelRegistry;
import com.vmware.ignite.common.DModel;
import com.vmware.vap.service.dto.JobStatusUpdateDTO;
import org.apache.commons.lang3.StringUtils;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

/**
 * The Ignite registry for maintaining endpoint state
 */
public class EndpointRegistry extends BaseModelRegistry<EndpointRegistry.Endpoint> {
    @Override
    protected Class getModelClass() {
        return Endpoint.class;
    }

    public void update(String endpointID, String endpointState, String jobId) {
        this.apply(endpointID, ep -> {
            ep.jobId = jobId;
            ep.computeStateAndStatus(endpointState);
            return ep;
        });
    }

    public void update(String endpointID, String endpointStatus) {
        this.apply(endpointID, ep -> {
            ep.computeStateAndStatus(endpointStatus);
            return ep;
        });
    }

    public void updateAgentStatus(String endpoint, String endpointAgentStatus){
        this.apply(endpoint, (ep) -> {
            ep.endpointAgentStatus = endpointAgentStatus;
            return ep;
        });
    }

    public JobStatusUpdateDTO.JobStatus getOverAllJobStatus(String vapName, String requestId) {
        // TODO: optimize to get only those records
        final JobStatusUpdateDTO.JobStatus[] overAllJobStatus = {JobStatusUpdateDTO.JobStatus.SUCCESS};

        this.applyToAll(ep -> {
            /*
            1. Set overall job status to
                a. INPROGRESS iff job is running for atleast one of the VMs
                b. FAILED iff job is running for atleast one of the VMs and completed for all of the VMs

             */
            if (ep.getJobId().equalsIgnoreCase(requestId)) {
                switch (ep.getState()) {
                    case Endpoint.STATUS_INSTALLING:
                    case Endpoint.STATUS_UNINSTALLING:
                    case Endpoint.STATUS_UNKNOWN:
                        overAllJobStatus[0] = JobStatusUpdateDTO.JobStatus.INPROGRESS;
                        break;
                    case Endpoint.STATUS_INSTALL_FAILED:
                    case Endpoint.STATUS_UNINSTALL_FAILED:
                        switch (overAllJobStatus[0]) {
                            case STARTING:
                            case INPROGRESS:
                                overAllJobStatus[0] = JobStatusUpdateDTO.JobStatus.INPROGRESS;
                                break;
                            case FAILED:
                            default:
                                overAllJobStatus[0] = JobStatusUpdateDTO.JobStatus.FAILED;
                        }
                        break;
                }
            }

            PrintUtils.printToActor("Job Status: [%s]", overAllJobStatus[0].toString());
            return overAllJobStatus[0];
        });

        return overAllJobStatus[0];
    }

    public static class Endpoint extends DModel {
        public static final String STATE_UNKNOWN = "Unknown";
        public static final String STATUS_UNKNOWN = "Unknown";
        public static final String STATE_INSTALLED = "Agent Installed";
        public static final String STATE_NOTINSTALLED = "Not Installed";
        public static final String STATUS_INSTALLING = "Install In Progress";
        public static final String STATUS_INSTALL_SUCCESS = "Install Successful";
        public static final String STATUS_INSTALL_FAILED = "Install Failed";
        public static final String STATUS_UNINSTALLING = "Uninstall In Progress";
        public static final String STATUS_UNINSTALL_SUCCESS = "Uninstall Successful";
        public static final String STATUS_UNINSTALL_FAILED = "Uninstall Failed";
        public static final String STATUS_STARTING = "Start In Progress";
        public static final String STATUS_START_SUCCESS = "Start Successful";
        public static final String STATUS_START_FAILED = "Start Failed";
        public static final String STATUS_STOPPING = "Stop In Progress";
        public static final String STATUS_STOP_SUCCESS = "Stop Successful";
        public static final String STATUS_STOP_FAILED = "Stop Failed";
        @QuerySqlField(index = true)
        private String vcUUID;
        @QuerySqlField
        private String vmMOR;
        @QuerySqlField
        private String endpointState;
        @QuerySqlField
        private String lastOperationStatus;
        @QuerySqlField
        private String endpointAgentStatus;
        @QuerySqlField
        private String user;
        @QuerySqlField
        private String password;
        @QuerySqlField
        private String jobId;

        public Endpoint(BootstrapActor.EndPointRegistryStateDTO dto) {
            super(dto);
            this.vcUUID = dto.getVc_id();
            this.vmMOR = dto.getVm_mor();
            this.endpointState = STATE_UNKNOWN;
            this.lastOperationStatus = STATUS_UNKNOWN;
            this.endpointAgentStatus = null;
            this.user = dto.getUser();
            this.password = dto.getPassword();
            this.jobId = null;
        }

        @Override
        public RegistryStateDTO toRegistryStateDTO() {
            return new BootstrapActor.EndPointRegistryStateDTO(this);
        }

        public String getState() {
            return this.endpointState;
        }

        public String getlastOperationStatus() {
            return this.lastOperationStatus;
        }

        public String getVcUUID() {
            return vcUUID;
        }

        public String getVmMOR() {
            return vmMOR;
        }

        public String getEndpointState() {
            return endpointState;
        }

        public String getUser() {
            return user;
        }

        public String getPassword() {
            return password;
        }

        public String getJobId() {
            return jobId;
        }

        public String getEndpointAgentStatus() {
            return endpointAgentStatus;
        }

        public void computeStateAndStatus(String newState) {

            if (newState != null && StringUtils.isNotEmpty(newState)) {
                String currentState = this.endpointState;
                String[] currentItems = newState.split(":");
                if (!currentState.equals(STATE_UNKNOWN)) {
                    String[] preItems = currentState.split(":");
                    int preStage = Integer.parseInt(preItems[2]);
                    int currentStage = Integer.parseInt(currentItems[2]);
                    if (currentStage < preStage) {
                        return;
                    }
                }

                if (!currentItems[1].equalsIgnoreCase(VapServiceConstants.IN_PROGRESS)) {
                    newState = currentItems[0] + "-" + currentItems[1];
                }

                this.endpointState = newState;
            }

            PrintUtils.printToActor("Endpoint Id: %s, state: %s",
              getVmMOR(),
              getState());
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(super.toString());
            sb.append(String.format("\t[VC_ID:%s] [VM_MOR:%s] [EP_STATE:%s] "
                + "[LAST_OP_STATUS:%s] [USER:%s] " +
                    "[PASSWORD:%s] [JOBID:%s]\n",
                this.getVcUUID(), this.getVmMOR(), this.getEndpointState(),
                this.getlastOperationStatus(), this.getUser(),
                this.getPassword(), this.getJobId()));
            return sb.toString();
        }
    }
}
