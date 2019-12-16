package com.vmware.apps.nfv.ignite;

import com.vmware.akka.common.RegistryStateDTO;
import com.vmware.ignite.common.BaseModelRegistry;
import com.vmware.ignite.common.DModel;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

public class FunctionRequestRegistry extends BaseModelRegistry<FunctionRequestRegistry.D_NFV_FuncReq> {
    @Override
    protected Class getModelClass() {
        return D_NFV_FuncReq.class;
    }

    @Override
    public Class getParentRegistryClass() {
        return OrderAcceptorRegistry.class;
    }

    public D_NFV_FuncReq useLicenses(String key, long lics) {
        return this.apply(key, cc -> cc.useLicenses(lics));
    }

    public D_NFV_FuncReq returnLicenses(String key, long lics) {
        return this.apply(key, cc -> cc.returnLicenses(lics));
    }

    public D_NFV_FuncReq returnAllLicenses(String key) { // key = vendor name/function
        return this.apply(key, D_NFV_FuncReq::returnAllLicenses);
    }

    /**
     * Model class for Data grid
     */
    public static class D_NFV_FuncReq extends DModel {
        @QuerySqlField
        private String vName;
        @QuerySqlField
        private String vFunc;
        @QuerySqlField
        private long boughtLics;
        @QuerySqlField
        private long availLics;

        public D_NFV_FuncReq(FuncReqRegDTO dto) {
            super(dto);
            this.vName = dto.getVName();
            this.vFunc = dto.getVFunc();
            this.boughtLics = dto.getBoughtLicenses();
            this.availLics = this.boughtLics;
        }

        @Override
        public RegistryStateDTO toRegistryStateDTO() {
            return new FuncReqRegDTO(this);
        }

        @Override
        public String toString() {
            return String.format("%30s %30s %30s %15d  %15d", getKey(), getVName(), getVFunc(),
                getBoughtLics(), getAvailLics());
        }

        public D_NFV_FuncReq useLicenses(long lics) {
            availLics = Math.max(availLics - lics, 0);
            return this;
        }

        public D_NFV_FuncReq returnLicenses(long lics) {
            availLics = Math.min(availLics + lics, getBoughtLics());
            return this;
        }

        public D_NFV_FuncReq returnAllLicenses() {
            availLics = getBoughtLics();
            return this;
        }

        public String getVName() {
            return vName;
        }

        public String getVFunc() {
            return vFunc;
        }

        public String getVRegId() {
            return getVName() + "/" + getVFunc();
        }

        public long getBoughtLics() {
            return boughtLics;
        }

        public long getAvailLics() {
            return availLics;
        }
    }

    public static class FuncReqRegDTO extends RegistryStateDTO {
        public static final String ATTR_V_NAME_KEY = "V_NAME_KEY";
        public static final String ATTR_V_FUNC_KEY = "V_FUNC_KEY";
        public static final String ATTR_BOUGHT_LICS_KEY = "BOUGHT_LICS_KEY";
        public static final String ATTR_AVAIL_LICS_KEY = "AVAIL_LICS_KEY";

        public FuncReqRegDTO(String oper, String name, String vName, String vFunc, long boughtLics) {
            super(FunctionRequestRegistry.class, oper, name);
            this.putAttr(ATTR_V_NAME_KEY, vName);
            this.putAttr(ATTR_V_FUNC_KEY, vFunc);
            this.putAttr(ATTR_BOUGHT_LICS_KEY, boughtLics);
        }

        public FuncReqRegDTO(D_NFV_FuncReq model) {
            super(FunctionRequestRegistry.class, model);
            this.putAttr(ATTR_V_NAME_KEY, model.getVName());
            this.putAttr(ATTR_V_FUNC_KEY, model.getVFunc());
            this.putAttr(ATTR_BOUGHT_LICS_KEY, model.getBoughtLics());
            this.putAttr(ATTR_AVAIL_LICS_KEY, model.getAvailLics());
        }

        @Override
        public DModel toModel() {
            return new D_NFV_FuncReq(this);
        }

        String getOperName() {
            return getParentKey();
        }

        String getFRName() {
            return getName();
        }

        String getVName() {
            return (String) getAttr(ATTR_V_NAME_KEY);
        }

        String getVFunc() {
            return (String) getAttr(ATTR_V_FUNC_KEY);
        }

        long getBoughtLicenses() {
            return (long) getAttr(ATTR_BOUGHT_LICS_KEY);
        }

        long getAvailLicenses() {
            return (long) getAttr(ATTR_AVAIL_LICS_KEY);
        }
    }
}

