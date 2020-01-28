package com.rad2.apps.nfv.ignite;

import com.rad2.akka.common.RegistryStateDTO;
import com.rad2.ignite.common.BaseModelRegistry;
import com.rad2.ignite.common.DModel;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

public class RelRegistry extends BaseModelRegistry<RelRegistry.D_NFV_RelModel> {
    @Override
    protected Class getModelClass() {
        return D_NFV_RelModel.class;
    }

    @Override
    public Class getParentRegistryClass() {
        return OrderAcceptorRegistry.class;
    }

    public D_NFV_RelModel reserveBW(String key, long bw) {
        return this.apply(key, cc -> cc.consumeBW(bw));
    }

    public D_NFV_RelModel returnBW(String key, long bw) {
        return this.apply(key, cc -> cc.returnBW(bw));
    }

    public void resetRelationship(String parentKey) {
        this.applyToChildrenOfParent(parentKey, D_NFV_RelModel::reset);
    }

    public static class D_NFV_RelModel extends DModel {
        @QuerySqlField
        private String dcReg1;
        @QuerySqlField
        private String dcReg2;
        @QuerySqlField
        private long maxBandwidth;
        @QuerySqlField
        private long availBandwidth;

        public D_NFV_RelModel(RelRegDTO dto) {
            super(dto);
            this.dcReg1 = dto.getDCReg1();
            this.dcReg2 = dto.getDCReg2();
            this.maxBandwidth = dto.getMaxBW();
            this.availBandwidth = this.maxBandwidth;
        }

        @Override
        public RegistryStateDTO toRegistryStateDTO() {
            return new RelRegDTO(this);
        }

        @Override
        public String toString() {
            return String.format("%45s %30s %30s %15d %15d", getKey(), getDcReg1(), getDcReg2(),
                getMaxBandwidth(), getAvailBandwidth());
        }

        public String getDcReg1() {
            return dcReg1;
        }

        public String getDcReg2() {
            return dcReg2;
        }

        public long getMaxBandwidth() {
            return maxBandwidth;
        }

        public long getAvailBandwidth() {
            return availBandwidth;
        }

        public D_NFV_RelModel consumeBW(long bw) {
            this.availBandwidth = Math.max(this.availBandwidth - bw, 0);
            return this;
        }

        public D_NFV_RelModel returnBW(long bw) {
            this.availBandwidth = Math.min(this.availBandwidth + bw, this.getMaxBandwidth());
            return this;
        }

        public D_NFV_RelModel reset() {
            this.availBandwidth = this.getMaxBandwidth();
            return this;
        }
    }

    public static class RelRegDTO extends RegistryStateDTO {
        public static final String ATTR_DC_REG1_KEY = "DC_REG1_KEY";
        public static final String ATTR_DC_REG2_KEY = "DC_REG2_KEY";
        public static final String ATTR_MAX_BW_KEY = "MAX_BW_KEY";
        public static final String ATTR_AVAIL_BW_KEY = "AVAIL_BW_KEY";

        public RelRegDTO(String parentKey, String name, String dcReg1, String dcReg2, long maxBW) {
            super(RelRegistry.class, parentKey, name);
            this.putAttr(ATTR_DC_REG1_KEY, dcReg1);
            this.putAttr(ATTR_DC_REG2_KEY, dcReg2);
            this.putAttr(ATTR_MAX_BW_KEY, maxBW);
        }

        public RelRegDTO(D_NFV_RelModel model) {
            super(RelRegistry.class, model);
            this.putAttr(ATTR_DC_REG1_KEY, model.getDcReg1());
            this.putAttr(ATTR_DC_REG2_KEY, model.getDcReg2());
            this.putAttr(ATTR_MAX_BW_KEY, model.getMaxBandwidth());
            this.putAttr(ATTR_AVAIL_BW_KEY, model.getAvailBandwidth());
        }

        @Override
        public DModel toModel() {
            return new D_NFV_RelModel(this);
        }

        String getDCReg1() {
            return (String) this.getAttr(ATTR_DC_REG1_KEY);
        }

        String getDCReg2() {
            return (String) this.getAttr(ATTR_DC_REG2_KEY);
        }

        long getMaxBW() {
            return (long) this.getAttr(ATTR_MAX_BW_KEY);
        }

        long getAvailBW() {
            return (long) this.getAttr(ATTR_AVAIL_BW_KEY);
        }
    }
}

