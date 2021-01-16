/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.apps.finco.ctrl;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rad2.akka.common.IDeferred;
import com.rad2.apps.finco.akka.FinCoWorker;
import com.rad2.ctrl.BaseController;

import java.util.ArrayList;
import java.util.List;

/**
 * The FinCoController acts as the central brain of the application, it receives
 * instructions from the FinCoResource and uses the AkkaActorSystemUtility and
 * the Apache Ignite Registry to perform those instructions. The Akka Actor
 * System performs the asynchronous, distributed, concurrent programming. All
 * shared state across Actors are held within the Ignite registry and is
 * available to all Actors on any Node in the cluster.
 */
public class FinCoController extends BaseController {
    public void addFinCo(FinCoListDTO dto) {
        ActorSelection fcr = getFinCoRouter();
        dto.getFinCoList().forEach(fc -> {
            fcr.tell(new FinCoWorker.AddFinCo(fc.name, fc.branch, fc.accountHolders), ActorRef.noSender());
        });
    }

    public void getAllBranches(IDeferred<String> req) {
        ActorSelection fcr = getFinCoRouter();
        fcr.tell(new FinCoWorker.GetAllBranches(req), ActorRef.noSender());
    }

    private ActorSelection getFinCoRouter() {
        return getAU().getActor(getAU().getLocalSystemName(), FinCoWorker.FINCO_MASTER_ROUTER);
    }

    @Override
    public List<Class> getDependenciesList() {
        List<Class> ret = new ArrayList<>();
        ret.add(FinCoInitializer.class);
        return ret;
    }

    /**
     * DTO's used by FinCoResource to communicate Data to FinCoController
     */
    public static class FinCoListDTO {
        private final List<FinCoDTO> finCoList;

        public FinCoListDTO() {
            this.finCoList = new ArrayList<>();
        }

        @JsonProperty
        public List<FinCoDTO> getFinCoList() {
            return finCoList;
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("{[");
            this.getFinCoList().forEach(x -> sb.append(x).append(","));
            sb.append("]}");
            return sb.toString();
        }

        public static class FinCoDTO {
            private String name; // the name of the Fin Co (e.g., Citibank)
            private String branch; // the LAX branch
            private final List<String> accountHolders;

            public FinCoDTO() {
                this.accountHolders = new ArrayList<>();
            }

            @JsonProperty
            public String getName() {
                return name;
            }

            @JsonProperty
            public String getBranch() {
                return branch;
            }

            @JsonProperty
            public List<String> getAccountHolders() {
                return accountHolders;
            }

            public String toString() {
                StringBuffer sb = new StringBuffer();
                sb.append("{")
                        .append("name: ").append(this.getName())
                        .append(", branch: ").append(this.getBranch())
                        .append(", accountHolders:[");
                this.getAccountHolders().forEach(x -> sb.append(x).append(","));
                sb.append("]}");
                return sb.toString();
            }
        }
    }
}
