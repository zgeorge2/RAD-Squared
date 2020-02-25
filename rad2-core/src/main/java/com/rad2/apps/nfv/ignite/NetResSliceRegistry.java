/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.apps.nfv.ignite;

import com.rad2.akka.common.RegistryStateDTO;
import com.rad2.ignite.common.BaseModelRegistry;
import com.rad2.ignite.common.DModel;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

public class NetResSliceRegistry extends BaseModelRegistry<NetResSliceRegistry.D_NFV_NetResSlice> {
    @Override
    protected Class getModelClass() {
        return D_NFV_NetResSlice.class;
    }

    @Override
    public Class getParentRegistryClass() {
        return OrderAcceptorRegistry.class;
    }

    /**
     * Model class for Data grid
     */
    public static class D_NFV_NetResSlice extends DModel {
        @QuerySqlField
        private String oper;
        @QuerySqlField
        private String resRegId;
        @QuerySqlField
        private long cpu;
        @QuerySqlField
        private long mem;
        @QuerySqlField
        private String funcReqRegId;
        @QuerySqlField
        private String vRegId;
        @QuerySqlField
        private long lics;

        public D_NFV_NetResSlice(NetResSliceRegDTO dto) {
            super(dto);
            this.oper = dto.getOper();
            this.resRegId = dto.getResRegId();
            this.cpu = dto.getCpu();
            this.mem = dto.getMem();
            this.funcReqRegId = dto.getFuncReqRegId();
            this.vRegId = dto.getVRegId();
            this.lics = dto.getLics();
        }

        @Override
        public RegistryStateDTO toRegistryStateDTO() {
            return new NetResSliceRegDTO(this);
        }

        @Override
        public String toString() {
            return String.format("%30s %30s %30s %15d %15d %30s %30s %15d", getKey(), getOper(),
                getResRegId(), getCpu(), getMem(),
                getFuncReqRegId(), getVRegId(), getLics());
        }

        public String getOper() {
            return oper;
        }

        public String getResRegId() {
            return resRegId;
        }

        public long getCpu() {
            return cpu;
        }

        public long getMem() {
            return mem;
        }

        public String getFuncReqRegId() {
            return funcReqRegId;
        }

        public String getVRegId() {
            return vRegId;
        }

        public long getLics() {
            return lics;
        }
    }

    public static class NetResSliceRegDTO extends RegistryStateDTO {
        public static final String ATTR_OPER_KEY = "OPER_KEY";
        public static final String ATTR_RES_REG_ID_KEY = "RES_REG_ID_KEY";
        public static final String ATTR_CPU_KEY = "CPU_KEY";
        public static final String ATTR_MEM_KEY = "MEM_KEY";
        public static final String ATTR_FUNC_REQ_REG_ID_KEY = "FUNC_REQ_REG_ID_KEY";
        public static final String ATTR_VENDOR_REG_ID_KEY = "VENDOR_REG_ID_KEY";
        public static final String ATTR_LICS_KEY = "LICS_KEY";

        public NetResSliceRegDTO(String id, String name, String oper,
                                 String resRegId, long cpu, long mem,
                                 String funcReqRegId, String vendorRegId, long lics) {
            super(NetResSliceRegistry.class, id, name);
            this.putAttr(ATTR_OPER_KEY, oper);
            this.putAttr(ATTR_RES_REG_ID_KEY, resRegId);
            this.putAttr(ATTR_CPU_KEY, cpu);
            this.putAttr(ATTR_MEM_KEY, mem);
            this.putAttr(ATTR_FUNC_REQ_REG_ID_KEY, funcReqRegId);
            this.putAttr(ATTR_VENDOR_REG_ID_KEY, vendorRegId);
            this.putAttr(ATTR_LICS_KEY, lics);
        }

        public NetResSliceRegDTO(D_NFV_NetResSlice model) {
            super(NetResSliceRegistry.class, model);
        }

        @Override
        public DModel toModel() {
            return new D_NFV_NetResSlice(this);
        }

        String getOper() {
            return (String) getAttr(ATTR_OPER_KEY);
        }

        String getResRegId() {
            return (String) getAttr(ATTR_RES_REG_ID_KEY);
        }

        long getCpu() {
            return (long) getAttr(ATTR_CPU_KEY);
        }

        long getMem() {
            return (long) getAttr(ATTR_MEM_KEY);
        }

        String getFuncReqRegId() {
            return (String) getAttr(ATTR_FUNC_REQ_REG_ID_KEY);
        }

        String getVRegId() {
            return (String) getAttr(ATTR_VENDOR_REG_ID_KEY);
        }

        long getLics() {
            return (long) getAttr(ATTR_LICS_KEY);
        }
    }
}

