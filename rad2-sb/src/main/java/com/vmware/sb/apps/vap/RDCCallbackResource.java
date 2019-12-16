package com.vmware.sb.apps.vap;

import com.vmware.common.constants.VapServiceConstants;
import com.vmware.apps.vap.ctrl.RDCCallbackController;
import com.vmware.sb.res.BaseResource;
import com.vmware.vap.bridge.common.MessageType;
import com.vmware.vap.bridge.common.VAPBridgeConstants;
import com.vmware.vap.service.dto.CallbackMessageDTO;
import com.vmware.vap.service.dto.CallbackResponse;
import org.springframework.web.bind.annotation.*;

/**
 * The RDCCallbackResource provides REST resources for use by internal entities such as Lemans to forward on-prem
 * messages into VAP SaaS Application This class, thus, provides the REST API entry point into the VAP SaaS Application.
 */
@RestController
@RequestMapping(VapServiceConstants.CALLBACK_URL)
@ResponseBody
public class RDCCallbackResource extends BaseResource<RDCCallbackController> {
    @RequestMapping()
    @ResponseBody
    public String defaultMethod() {
        return "DEFAULT HANDLING";
    }

    @RequestMapping("/test")
    @ResponseBody
    public String fallbackMethod() {
        return "fallback method";
    }

    @PostMapping(path = VapServiceConstants.CONTROLPLANE_ACTION_PREFIX)
    public CallbackResponse controlPlaneMessageReceiver(
                @RequestBody String payload,
                @RequestHeader(VAPBridgeConstants.VAP_MESSAGE_TYPE) MessageType messageType,
                @RequestHeader(VAPBridgeConstants.VAP_RDC_ID) String rdcId,
                @RequestHeader(VAPBridgeConstants.LM_TENANT_ID) String tenantId,
                @RequestHeader(VAPBridgeConstants.LM_AGENT_ID) String agentId
    ) {
        this.getC().handleCallbackMessage(
                    new CallbackMessageDTO(messageType, payload,
                                new CallbackMessageDTO.CallbackMessageHeader(
                                            tenantId, rdcId, agentId)));
        return new CallbackResponse(
                    "Submitted metrics for persistence vapName=");

    }

    @PostMapping(path = VapServiceConstants.ENDPOINTSTATE_PREFIX)
    public CallbackResponse endpointStateMessageReceiver(
                @RequestBody String payload,
                @RequestHeader(VAPBridgeConstants.VAP_MESSAGE_TYPE) MessageType messageType,
                @RequestHeader(VAPBridgeConstants.VAP_RDC_ID) String rdcId,
                @RequestHeader(VAPBridgeConstants.LM_TENANT_ID) String tenantId,
                @RequestHeader(VAPBridgeConstants.LM_AGENT_ID) String agentId) {
        this.getC().handleCallbackMessage(
                    new CallbackMessageDTO(messageType, payload,
                                new CallbackMessageDTO.CallbackMessageHeader(
                                            tenantId, rdcId, agentId)));
        return new CallbackResponse(
                    "Submitted metrics for persistence vapName=");
    }

    @PostMapping(path = VapServiceConstants.VAPSTATE_PREFIX)
    public CallbackResponse vapStateMessageReceiver(
                @RequestBody String payload,
                @RequestHeader(VAPBridgeConstants.VAP_MESSAGE_TYPE) MessageType messageType,
                @RequestHeader(VAPBridgeConstants.VAP_RDC_ID) String rdcId,
                @RequestHeader(VAPBridgeConstants.LM_TENANT_ID) String tenantId,
                @RequestHeader(VAPBridgeConstants.LM_AGENT_ID) String agentId) {
        this.getC().handleCallbackMessage(
                    new CallbackMessageDTO(messageType, payload,
                                new CallbackMessageDTO.CallbackMessageHeader(
                                            tenantId, rdcId, agentId)));
        return new CallbackResponse(
                    "Submitted metrics for persistence vapName=");
    }

    @PostMapping(path = VapServiceConstants.PROCESS_PREFIX)
    public CallbackResponse processReceiver(
                @RequestBody String payload,
                @RequestHeader(VAPBridgeConstants.VAP_MESSAGE_TYPE) MessageType messageType,
                @RequestHeader(VAPBridgeConstants.VAP_RDC_ID) String rdcId,
                @RequestHeader(VAPBridgeConstants.LM_TENANT_ID) String tenantId,
                @RequestHeader(VAPBridgeConstants.LM_AGENT_ID) String agentId) {
        this.getC().handleCallbackMessage(
                    new CallbackMessageDTO(messageType, payload,
                                new CallbackMessageDTO.CallbackMessageHeader(
                                            tenantId, rdcId, agentId)));
        return new CallbackResponse(
                    "Submitted metrics for persistence vapName=" + agentId);
    }
}
