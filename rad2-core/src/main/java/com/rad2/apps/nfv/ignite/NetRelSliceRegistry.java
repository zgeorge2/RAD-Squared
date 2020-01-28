package com.rad2.apps.nfv.ignite;

import com.rad2.akka.common.RegistryStateDTO;
import com.rad2.ignite.common.BaseModelRegistry;
import com.rad2.ignite.common.DModel;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

public class NetRelSliceRegistry extends BaseModelRegistry<NetRelSliceRegistry.D_NFV_NetRelSlice> {
    @Override
    protected Class getModelClass() {
        return D_NFV_NetRelSlice.class;
    }

    @Override
    public Class getParentRegistryClass() {
        return OrderAcceptorRegistry.class;
    }

    /**
     * Model class for Data grid
     */
    public static class D_NFV_NetRelSlice extends DModel {
        @QuerySqlField
        private String oper;
        @QuerySqlField
        private String relRegId;
        @QuerySqlField
        private long bandwidth;

        public D_NFV_NetRelSlice(NetRelSliceRegDTO dto) {
            super(dto);
            this.oper = dto.getOper();
            this.relRegId = dto.getRelRegId();
            this.bandwidth = dto.getBandwidth();
        }

        @Override
        public RegistryStateDTO toRegistryStateDTO() {
            return new NetRelSliceRegDTO(this);
        }

        @Override
        public String toString() {
            return String.format("%30s %30s %30s %30d", getKey(), getOper(), getRelRegId(), getBandwidth());
        }

        public String getOper() {
            return oper;
        }

        public String getRelRegId() {
            return relRegId;
        }

        public long getBandwidth() {
            return bandwidth;
        }
    }

    public static class NetRelSliceRegDTO extends RegistryStateDTO {
        public static final String ATTR_OPER_KEY = "OPER_KEY";
        public static final String ATTR_REL_REG_ID_KEY = "REL_REG_ID_KEY";
        public static final String ATTR_BW_KEY = "BW_KEY";

        public NetRelSliceRegDTO(String id, String name, String oper, String relRegId, long bw) {
            super(NetRelSliceRegistry.class, id, name);
            this.putAttr(ATTR_OPER_KEY, oper);
            this.putAttr(ATTR_REL_REG_ID_KEY, relRegId);
            this.putAttr(ATTR_BW_KEY, bw);
        }

        public NetRelSliceRegDTO(D_NFV_NetRelSlice model) {
            super(NetRelSliceRegistry.class, model);
        }

        @Override
        public DModel toModel() {
            return new D_NFV_NetRelSlice(this);
        }

        String getOper() {
            return (String) getAttr(ATTR_OPER_KEY);
        }

        String getRelRegId() {
            return (String) getAttr(ATTR_REL_REG_ID_KEY);
        }

        long getBandwidth() {
            return (long) getAttr(ATTR_BW_KEY);
        }
    }
}

