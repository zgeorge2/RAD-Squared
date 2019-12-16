package com.vmware.apps.vap.akka;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.reflect.TypeToken;
import com.vmware.akka.common.BaseActor;
import com.vmware.apps.vap.akka.dto.RDCStateRegistryDto;
import com.vmware.common.metrics.MetricsStreamParser;
import com.vmware.ignite.common.RegistryManager;
import com.vmware.apps.vap.ignite.RDCStateRegistry;
import com.vmware.apps.vap.ignite.RDCStateRegistry.RDCState;
import com.vmware.vap.service.VapServiceUtils;
import com.vmware.vap.service.dto.CallbackMessageDTO;
import com.vmware.vap.service.message.ActorMessage;

import akka.actor.Props;

/**
 * Actor to act on messages relating to the state of the RDC itself this includes 1) Health of the
 * Overall RDC 2) Health of various components in the RDC 3) Version and other state of the RDC
 *
 */
public class RDCStateActor extends BaseActor {

    private static final String VAP_RDC_OVERALL_HEALTH_METRIC =
            "ucp.health.status";

    private static final String VAP_RDC_COMPONENT_HEALTH_METRIC_PREFIX =
            "ucp.health.component";


    protected RDCStateActor(RegistryManager rm) {
        super(rm);
    }

    static public Props props(RegistryManager rm) {
        return Props.create(RDCStateActor.class, rm);
    }

    @Override
    public Receive createReceive() {
        return super.createReceive().orElse(receiveBuilder()
                .match(RDCHealth.class, this::handleRDCHealthData).build());
    }

    public void handleRDCHealthData(RDCHealth healthMetrics) {

        String payload = healthMetrics.getPayload();
        RDCStateRegistry rdcStateRegistry = reg(RDCStateRegistry.class);

        List<MetricsStreamParser.MetricLineParts> metricLineParts =
                MetricsStreamParser.parsePayload(payload);

        String rdcID = healthMetrics.getRdcId();
        String rdcIP = "";
        int overallHealth = 0;
        String timeStamp = String.valueOf(System.currentTimeMillis());
        Map<String, Integer> componentHealth = new HashMap<>();

        for (final MetricsStreamParser.MetricLineParts metricLinePart : metricLineParts) {
            if (metricLinePart.getMetricName()
                    .startsWith(VAP_RDC_COMPONENT_HEALTH_METRIC_PREFIX)) {
                String componentName = metricLinePart.getMetricName().substring(
                        VAP_RDC_COMPONENT_HEALTH_METRIC_PREFIX.length()+1);
                int health = (int) metricLinePart.getMetricValue();
                componentHealth.put(componentName, health);
            } else if (metricLinePart.getMetricName()
                    .equals(VAP_RDC_OVERALL_HEALTH_METRIC)) {
                overallHealth = (int) metricLinePart.getMetricValue();
                timeStamp = metricLinePart.getTimestamp();
                rdcIP = metricLinePart.getSource();
            }
        }
        String componentHealthStr =
                VapServiceUtils.convertToJson(componentHealth,
                        new TypeToken<Map<String, Integer>>() {}.getType());

        RDCState oldState = rdcStateRegistry.getRDCStateForID(rdcID);
        RDCStateRegistryDto dto = new RDCStateRegistryDto(rdcID, rdcIP);
        dto.setRdcHealthStatus(overallHealth);
        dto.setRdcComponentHealth(componentHealthStr);
        dto.setLastUpdatedTime(timeStamp);

        if(null==oldState){
            rdcStateRegistry.add(dto);
        } else {
            rdcStateRegistry.update(dto);
        }
    }

    public static class RDCHealth extends ActorMessage {
        public RDCHealth(CallbackMessageDTO callbackMessageDTO) {
            super(callbackMessageDTO);
        }
    }


}
