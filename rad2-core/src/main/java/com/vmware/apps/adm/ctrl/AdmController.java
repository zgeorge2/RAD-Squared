package com.vmware.apps.adm.ctrl;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vmware.akka.router.MasterRouter;
import com.vmware.apps.adm.akka.NodeAdmin;
import com.vmware.ctrl.BaseController;
import java.util.ArrayList;
import java.util.List;
/**
 *
 */
public class AdmController extends BaseController {
    public void shutdown() {
        this.getNodeAdmin().tell(new NodeAdmin.ShutdownNode(true), ActorRef.noSender());
    }
    public void increaseRoutees(IncreaseRouteesDTO dto) {
        this.getRouter(dto.getSystem(), dto.getRouter()).tell(new MasterRouter.IncreaseRoutees(), ActorRef.noSender());
    }
    public void removeeRoutees(IncreaseRouteesDTO dto) {
        this.getRouter(dto.getSystem(), dto.getRouter()).tell(new MasterRouter.RemoveRoutees(), ActorRef.noSender());
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
    private ActorSelection getRouter(String systemName, String routerName) {
        return getAU().getActor(systemName, routerName);
    }
    public static class IncreaseRouteesDTO {
        private String router;
        private String system;
        @JsonProperty
        public String getRouter() {
            return router;
        }
        @JsonProperty
        public String getSystem() {
            return system;
        }
    }
}