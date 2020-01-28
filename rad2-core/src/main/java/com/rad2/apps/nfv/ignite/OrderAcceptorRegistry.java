package com.rad2.apps.nfv.ignite;

import com.rad2.akka.common.RegistryStateDTO;
import com.rad2.apps.nfv.akka.OrderAcceptor;
import com.rad2.ignite.common.BaseModelRegistry;
import com.rad2.ignite.common.DModel;

public class OrderAcceptorRegistry extends BaseModelRegistry<OrderAcceptorRegistry.D_NFV_OrderAcceptorModel> {
    @Override
    protected Class getModelClass() {
        return D_NFV_OrderAcceptorModel.class;
    }

    public static class D_NFV_OrderAcceptorModel extends DModel {
        public D_NFV_OrderAcceptorModel(OrderAcceptorRegDTO dto) {
            super(dto);
        }

        @Override
        public RegistryStateDTO toRegistryStateDTO() {
            return new OrderAcceptorRegDTO(this);
        }

        @Override
        public Class getActorClass() {
            return OrderAcceptor.class;
        }
    }

    public static class OrderAcceptorRegDTO extends RegistryStateDTO {
        public OrderAcceptorRegDTO(String parentKey, String name) {
            super(OrderAcceptorRegistry.class, parentKey, name);
        }

        public OrderAcceptorRegDTO(D_NFV_OrderAcceptorModel model) {
            super(OrderAcceptorRegistry.class, model);
        }

        @Override
        public DModel toModel() {
            return new D_NFV_OrderAcceptorModel(this);
        }
    }
}
