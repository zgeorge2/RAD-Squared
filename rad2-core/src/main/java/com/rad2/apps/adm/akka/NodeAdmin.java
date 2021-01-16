/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.apps.adm.akka;

import akka.actor.Props;
import com.rad2.akka.common.BaseActor;
import com.rad2.common.serialization.IAkkaSerializable;
import com.rad2.common.utils.PrintUtils;
import com.rad2.ignite.common.RegistryManager;

public class NodeAdmin extends BaseActor {
    public static String NODE_ADMIN_NAME = "NODE_ADMIN_NAME";

    private NodeAdmin(RegistryManager rm) {
        super(rm);
    }

    static public Props props(RegistryManager rm) {
        return Props.create(NodeAdmin.class, rm);
    }

    @Override
    public Receive createReceive() {
        return super.createReceive()
            .orElse(receiveBuilder()
                .match(ShutdownNode.class, this::shutdownNode)
                .build());
    }

    private void shutdownNode(ShutdownNode arg) {
        if (arg.forward) {
            // tell all other Node Admins to shutdown
            this.getAU().getActorInAllRemoteSystems(NODE_ADMIN_NAME)
                .forEach(a -> a.tell(new ShutdownNode(false), self()));
        }
        PrintUtils.print("Shutting down RAD node: [%s]", this.getAU().getLocalSystemName());
        this.getRM().shutdownRegistry(); // shutdown ignite
        this.getAU().terminate(); // shutdown the actor system & this process
    }

    /**
     * Classes used for receive method above.
     */
    static public class ShutdownNode implements IAkkaSerializable {
        public boolean forward; // whether to forward this request to other nodes

        public ShutdownNode() {
            this(false);
        }

        public ShutdownNode(boolean forward) {
            this.forward = forward;
        }
    }
}

