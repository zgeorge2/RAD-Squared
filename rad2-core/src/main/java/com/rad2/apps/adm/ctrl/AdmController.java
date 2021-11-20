/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.apps.adm.ctrl;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rad2.akka.router.MasterRouter;
import com.rad2.apps.adm.akka.NodeAdmin;
import com.rad2.common.utils.PrintUtils;
import com.rad2.ctrl.BaseController;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for administrative actions that apply across all applications. Do NOT place application specific
 * functions here.
 */
public class AdmController extends BaseController {
    public void shutdown() {
        this.getNodeAdmin().tell(new NodeAdmin.ShutdownNode(true), ActorRef.noSender());
    }

    public void increaseRoutees(UpdateRouteesDTO dto) {
        this.getRouter(dto.getSystem(), dto.getRouter()).tell(new MasterRouter.IncreaseRoutees(), ActorRef.noSender());
    }

    public void removeRoutees(UpdateRouteesDTO dto) {
        this.getRouter(dto.getSystem(), dto.getRouter()).tell(new MasterRouter.RemoveRoutees(), ActorRef.noSender());
    }

    public void updateMultipleRoutees(List<UpdateRouteesDTO> dtos) {
        for(UpdateRouteesDTO dto: dtos) {
            ActorSelection sel = this.getRouter(dto.getSystem(), dto.getRouter());

            if(sel != null) {
                sel.tell(new MasterRouter.UpdateRoutees(dto.getFinalCount()), ActorRef.noSender());
            }
        }
    }

    private ActorSelection getNodeAdmin() {
        return getAU().getActor(getAU().getLocalSystemName(), NodeAdmin.NODE_ADMIN_NAME);
    }

    @Override
    public List<Class> getDependenciesList() {
        List<Class> ret = new ArrayList<>();
        ret.add(AdmAppInitializer.class);
        return ret;
    }

    public static class UpdateRouteesDTO {
        private String router;
        private String system;
        private int    finalCount;

        @JsonProperty
        public String getRouter() {
            return router;
        }

        @JsonProperty
        public String getSystem() {
            return system;
        }

        @JsonProperty
        public int getFinalCount() {
            return finalCount;
        }
    }
}