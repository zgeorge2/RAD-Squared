package com.vmware.apps.vap.ignite;

import com.vmware.apps.vap.akka.BootstrapActor;
import com.vmware.ignite.common.BaseModelRegistry;
import com.vmware.apps.vap.ignite.EndpointRegistry.Endpoint;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

public class EndpointHistoryRegistry extends BaseModelRegistry<EndpointHistoryRegistry.EndpointHistory> {
    @Override
    protected Class getModelClass() {
        return EndpointHistory.class;
    }

    @Override
    public Class getParentRegistryClass() {
        return EndpointRegistry.class;
    }

    public static class EndpointHistory extends Endpoint {
        @QuerySqlField
        private long timeStamp;

        public EndpointHistory(BootstrapActor.EndPointRegistryStateDTO dto) {
            super(dto);
            this.timeStamp = System.currentTimeMillis();
        }

        public long getTimeStamp() {
            return this.timeStamp;
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(super.toString());
            sb.append(String.format("\t[TIMESTAMP:%s]\n", this.getTimeStamp()));
            return sb.toString();
        }
    }
}
