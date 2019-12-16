package com.vmware.vap.service.control;

import com.vmware.ctrl.ControllerDependency;
import com.vmware.ignite.util.JobType;
import com.vmware.vap.service.dto.*;
import com.vmware.vap.service.exception.LemansServiceException;

import java.util.UUID;

/**
 * The interface that defines the methods to be provided by the On Prem Vap Controller to make calls to the On
 * Prem Vap Instance
 */
public interface OnPremVapDelegate extends ControllerDependency {
    void bootstrapEndpoint(UUID requestID, AgentDeploymentRequest agentDetails) throws LemansServiceException;

   // JobStatusUpdateDTO initializeJobWithPeriodicCheck(UUID requestID, VapDTO vapInstance);

    <T> T checkJobStatus(UUID requestID, String  agentId, JobType<T> jobType) throws LemansServiceException;

    void managePlugins(UUID requestID, PluginsDTO pluginsDTO) throws LemansServiceException;

    void manageAgent(UUID requestID, AgentManagementRequest agentManagementDTO) throws LemansServiceException;

    ValidateCloudAccountResponse validateCloudAccount(UUID requestId, ValidateCloudAccountRequest validateCloudAccountRequest) throws
            LemansServiceException;

}
