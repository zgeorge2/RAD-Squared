package com.vmware.apps.vap.akka;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.vmware.akka.common.BaseActorWithTimer;
import com.vmware.ignite.common.RegistryManager;
import com.vmware.apps.vap.ignite.EndpointRegistry;
import com.vmware.apps.vap.ignite.ServiceRegistry;
import com.vmware.common.constants.VapServiceConstants;
import com.vmware.vap.service.VapServiceUtils;
import com.vmware.vap.service.dto.EndpointDTO;
import com.vmware.vap.service.dto.EndpointSearchItemDTO;
import com.vmware.vap.service.dto.EndpointSearchRequestDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Actor used to make retirveal actions in SAAS.
 */
public class VapRetrieverActor extends BaseActorWithTimer {

    protected VapRetrieverActor(RegistryManager rm) {
        super(rm);
    }

    public static Props props(RegistryManager rm) {
        return Props.create(VapRetrieverActor.class, rm);
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return super.createReceive().orElse(
                    receiveBuilder().match(EndpointRetreiver.class, this::retreiveEndpoints).build());
    }

    private void retreiveEndpoints(EndpointRetreiver endpointRetreiver) {
        EndpointRegistry endpointRegistry = reg(EndpointRegistry.class);
        ServiceRegistry serviceRegistry = reg(ServiceRegistry.class);
        Map<String, List<String>> vcVMMap = getVCAndVmMapping(endpointRetreiver.endpointSearchRequestDTO);
        List<EndpointRegistry.Endpoint> endpointList = endpointRegistry.fetchResultList(
                    constructQuery(vcVMMap), contructArgs(vcVMMap));
        List<EndpointDTO> endpointDTOList = new ArrayList<>();
        for(final EndpointRegistry.Endpoint endpoint : endpointList){
            EndpointDTO endpointDTO = new EndpointDTO();
            endpointDTO.setVcUUID(endpoint.getVcUUID());
            endpointDTO.setVmMor(endpoint.getVmMOR());
            //endpointDTO.setLastOperationStatus();
            List<ServiceRegistry.DService> services = serviceRegistry
              .getServicesByVm(endpoint.getVcUUID(), endpoint.getVmMOR());
            if(services!=null && services.size()>0){
                endpointDTO.setServiceDtos(services);
            }
            endpointDTOList.add(endpointDTO);
        }
        sender().tell(endpointDTOList, this.getSelf());
    }

    private Object[] contructArgs(Map<String, List<String>> vcVMMap) {
        List<Object> objectsList = new ArrayList<>();
        for(final String vcId : vcVMMap.keySet()){
            objectsList.add(vcId);
            objectsList.addAll(vcVMMap.get(vcId));
        }
        return objectsList.toArray();
    }

    private String constructQuery(Map<String, List<String>> vcVMMap) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (final String vcId : vcVMMap.keySet()) {
            if (i > 0) {
                sb.append("or");
            }
            List<String> vms = vcVMMap.get(vcId);
            sb.append("(" + VapServiceConstants.VC_UUID + " = ? and " + VapServiceConstants.VM_MOR + " in "
                        + VapServiceUtils.buildQuestionString(vms.size()) + ")");
            i++;
        }
        return sb.toString();

    }


    private Map<String, List<String>> getVCAndVmMapping(EndpointSearchRequestDTO endpointSearchRequestDTO) {
        Map<String, List<String>> vcVmMap = new HashMap<>();
        if (null != endpointSearchRequestDTO) {
            for (final EndpointSearchItemDTO endpointSearchItemDTO : endpointSearchRequestDTO.getEndpointSearchItemDTOList()) {
                String vcId = endpointSearchItemDTO.getVcUUID();
                if (!vcVmMap.containsKey(vcId)) {
                    vcVmMap.put(vcId, endpointSearchItemDTO.getVmMors());
                }
            }
        }
        return vcVmMap;
    }

    public static class EndpointRetreiver {
        private EndpointSearchRequestDTO endpointSearchRequestDTO;

        public EndpointRetreiver(EndpointSearchRequestDTO endpointSearchRequestDTO) {
            this.endpointSearchRequestDTO = endpointSearchRequestDTO;
        }
    }

}
