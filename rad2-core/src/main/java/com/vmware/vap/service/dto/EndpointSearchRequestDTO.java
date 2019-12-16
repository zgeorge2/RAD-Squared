package com.vmware.vap.service.dto;

import java.util.List;

public class EndpointSearchRequestDTO {

    private List<EndpointSearchItemDTO> endpointSearchItemDTOList;

    public List<EndpointSearchItemDTO> getEndpointSearchItemDTOList() {
        return endpointSearchItemDTOList;
    }

    public void setEndpointSearchItemDTOList(List<EndpointSearchItemDTO> endpointSearchItemDTOList) {
        this.endpointSearchItemDTOList = endpointSearchItemDTOList;
    }


}
