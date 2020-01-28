package com.rad2.apps.nfv.akka;

import akka.actor.Props;
import com.rad2.akka.common.BaseActorWithRegState;
import com.rad2.akka.common.RegistryStateDTO;
import com.rad2.apps.nfv.ignite.OrderAcceptorRegistry;
import com.rad2.ignite.common.RegistryManager;

public class OrderAcceptor extends BaseActorWithRegState {
    private OrderAcceptor(RegistryManager rm, RegistryStateDTO dto) {
        super(rm, dto);
    }

    static public Props props(RegistryManager rm, String parentKey, String name) {
        RegistryStateDTO dto = new OrderAcceptorRegistry.OrderAcceptorRegDTO(parentKey, name);
        return Props.create(OrderAcceptor.class, rm, dto);
    }

    @Override
    public Receive createReceive() {
        return super.createReceive()
            .orElse(receiveBuilder()
                .match(CreateOrder.class, this::createOrder)
                .build());
    }

    private void createOrder(CreateOrder arg) {

    }

    /**
     * Classes used for receive method above.
     */
    static public class CreateOrder {
        String name;

        public CreateOrder(String name) {
            this.name = name;
        }
    }

}

