package com.vmware.apps.vap.akka;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.vmware.akka.common.BaseActor;
import com.vmware.akka.common.RegistryStateDTO;
import com.vmware.apps.vap.akka.dto.ServiceRegistryDto;
import com.vmware.apps.vap.ignite.ServiceRegistry;
import com.vmware.common.metrics.MetricsStreamParser;
import com.vmware.common.utils.PrintUtils;
import com.vmware.ignite.common.RegistryManager;
import com.vmware.vap.service.VapServiceUtils;
import com.vmware.vap.service.dto.CallbackMessageDTO;
import com.vmware.vap.service.message.ActorMessage;
import io.jsonwebtoken.lang.Collections;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;

/**
 * ProcessesActor to handle service discovery messages received on callback API.
 */
public class ProcessesActor extends BaseActor {
    private ProcessesActor(RegistryManager rm) {
        super(rm);
    }

    static public Props props(RegistryManager rm) {
        return Props.create(ProcessesActor.class, rm);
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return super.createReceive().orElse(
            receiveBuilder()
                .match(HandleProcessesMessage.class, this::handleProcessesMessage)
                .build());
    }

    /**
     * Handles below call back messages. Refer {@link com.vmware.vap.service.control.VapActorType} for more
     * information. 1. Processes
     */
    private void handleProcessesMessage(HandleProcessesMessage message) {
        PrintUtils.printToActor("Received message %s for processing", message.getMessageType());

        MetricsStreamParser parser = new MetricsStreamParser();
        List<MetricsStreamParser.MetricLineParts> metrics =
            parser.xtractMetricLineParts(message.getPayload());
        MetricsStreamParser.MetricLineParts countMetric = metrics.remove(0);

        String vcUUID = countMetric.getVc_uuid();
        String vmMOR = countMetric.getVm_mor();
        String vmID = VapServiceUtils.contructEndpointID(vcUUID, vmMOR);

        ServiceRegistry reg = this.getServiceRegistry();
        Map<String, ServiceRegistry.DService> serviceMapFromReg = getServiceMap(reg.getServicesByVm(vcUUID,
            vmMOR));

        for (MetricsStreamParser.MetricLineParts processMetric : metrics) {
            String serviceDisplayName = processMetric.getKeyValuePairs().get("display_name");
            if (!"VAP_NA".equals(serviceDisplayName) && !serviceDisplayName.endsWith(" - " +
                "child")) {
                String serviceName = processMetric.getKeyValuePairs().get("internal_key");
                ServiceRegistry.DService serviceInRegistry =
                    serviceMapFromReg.remove(serviceName);
                if (serviceInRegistry == null) {
                    reg.add(createServiceRegistryDto(serviceName, vmID,
                        processMetric.getKeyValuePairs()));
                } else if (serviceInRegistry.getStartTime() < Long.parseLong(processMetric.getKeyValuePairs().get(
                    "starttime"))) {
                    ServiceRegistryDto serviceRegistryDto =
                        serviceInRegistry.toRegistryStateDTO();
                    reg.update(updateServiceRegistryDto(serviceRegistryDto,
                        processMetric.getKeyValuePairs()));
                }
            }
        }

        //services remaining in serviceMapFromReg means they are not running at the moment. Hence remove
        // from registry.
        for (ServiceRegistry.DService service : serviceMapFromReg.values()) {
            reg.remove(service);
        }
    }

    private RegistryStateDTO updateServiceRegistryDto(ServiceRegistryDto serviceRegistryDto,
                                                      Map<String
                                                          , String> keyValuePairs) {
        if (serviceRegistryDto == null) {
            return null;
        }

        serviceRegistryDto.setPid(Integer.parseInt(keyValuePairs.get("pid")));
        serviceRegistryDto.setPpid(Integer.parseInt(keyValuePairs.get("ppid")));
        serviceRegistryDto.setCategory(keyValuePairs.get("category"));
        serviceRegistryDto.setCommand(keyValuePairs.get("process"));
        serviceRegistryDto.setDisplayName(keyValuePairs.get("display_name"));
        serviceRegistryDto.setGroupKey(keyValuePairs.get("group_key"));
        serviceRegistryDto.setInstallPath(keyValuePairs.get("install_path"));
        serviceRegistryDto.setStarted(Boolean.parseBoolean(keyValuePairs.get("started")));
        serviceRegistryDto.setStartMode(keyValuePairs.get("startMode"));
        serviceRegistryDto.setStartTime(Long.parseLong(keyValuePairs.get("starttime")));
        serviceRegistryDto.setVendor(keyValuePairs.get("vendor"));
        serviceRegistryDto.setUserName(keyValuePairs.get("username"));
        serviceRegistryDto.setVersion(keyValuePairs.get("version"));
        serviceRegistryDto.setPorts(keyValuePairs.get("ports"));

        return serviceRegistryDto;
    }

    private RegistryStateDTO createServiceRegistryDto(String serviceName, String vmID,
                                                      Map<String, String> keyValuePairs) {

        ServiceRegistryDto serviceRegistryDto = new ServiceRegistryDto(serviceName,
            vmID);
        return updateServiceRegistryDto(serviceRegistryDto, keyValuePairs);
    }

    private Map<String, ServiceRegistry.DService> getServiceMap(List<ServiceRegistry.DService> services) {

        if (Collections.isEmpty(services)) {
            return emptyMap();
        }

        Map<String, ServiceRegistry.DService> serviceMap = new HashMap<>();
        for (ServiceRegistry.DService service : services) {
            serviceMap.put(service.getServiceName(), service);
        }
        return serviceMap;
    }

    private ServiceRegistry getServiceRegistry() {
        return this.reg(ServiceRegistry.class);
    }

    public static class HandleProcessesMessage extends ActorMessage {
        public HandleProcessesMessage(CallbackMessageDTO messageDTO) {
            super(messageDTO);
        }
    }
}
