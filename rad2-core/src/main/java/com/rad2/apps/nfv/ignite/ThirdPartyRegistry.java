/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.apps.nfv.ignite;

import com.rad2.akka.common.RegistryStateDTO;
import com.rad2.ignite.common.BaseModelRegistry;
import com.rad2.ignite.common.DModel;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

public class ThirdPartyRegistry extends BaseModelRegistry<ThirdPartyRegistry.D_NFV_ThirdPartyModel> {
    @Override
    protected Class getModelClass() {
        return D_NFV_ThirdPartyModel.class;
    }

    @Override
    public Class getParentRegistryClass() {
        return OrderAcceptorRegistry.class;
    }

    public D_NFV_ThirdPartyModel buyLicenses(String key, long lics) {
        return this.apply(key, cc -> cc.buyLicenses(lics));
    }

    public D_NFV_ThirdPartyModel returnLicenses(String key, long lics) {
        return this.apply(key, cc -> cc.returnLicenses(lics));
    }

    public D_NFV_ThirdPartyModel returnAllLicenses(String key) { // key = vendor name/function
        return this.apply(key, D_NFV_ThirdPartyModel::returnAllLicenses);
    }

    /**
     * Model class for Data grid
     */
    public static class D_NFV_ThirdPartyModel extends DModel {
        @QuerySqlField
        private long cpuPer;
        @QuerySqlField
        private long memPer;
        @QuerySqlField
        private long maxLics;
        @QuerySqlField
        private long availLics;

        public D_NFV_ThirdPartyModel(ThirdPartyRegDTO dto) {
            super(dto);
            this.cpuPer = dto.getCPUPer();
            this.memPer = dto.getMemPer();
            this.maxLics = dto.getMaxLicenses();
            this.availLics = this.maxLics;
        }

        @Override
        public RegistryStateDTO toRegistryStateDTO() {
            return new ThirdPartyRegDTO(this);
        }

        @Override
        public String toString() {
            return String.format("%30s %15d %15d %15d %15d", getKey(), getCpuPer(), getMemPer(),
                getMaxLics(), getAvailLics());
        }

        String getVendorName() {
            return getParentKey();
        }

        String getFunction() {
            return getName();
        }

        public long getCpuPer() {
            return cpuPer;
        }

        public long getMemPer() {
            return memPer;
        }

        public long getMaxLics() {
            return maxLics;
        }

        public long getAvailLics() {
            return availLics;
        }

        public D_NFV_ThirdPartyModel buyLicenses(long lics) {
            availLics = Math.max(availLics - lics, 0);
            return this;
        }

        public D_NFV_ThirdPartyModel returnLicenses(long lics) {
            availLics = Math.min(availLics + lics, getMaxLics());
            return this;
        }

        public D_NFV_ThirdPartyModel returnAllLicenses() {
            availLics = getMaxLics();
            return this;
        }
    }

    public static class ThirdPartyRegDTO extends RegistryStateDTO {
        public static final String ATTR_CPU_PER_KEY = "CPU_PER_KEY";
        public static final String ATTR_MEM_PER_KEY = "MEM_PER_KEY";
        public static final String ATTR_MAX_LICS_KEY = "MAX_LICS_KEY";
        public static final String ATTR_AVAIL_LICS_KEY = "AVAIL_LICS_KEY";

        public ThirdPartyRegDTO(String name, String function, long cpuPer, long memPer, long maxLics) {
            super(ThirdPartyRegistry.class, name, function);
            this.putAttr(ATTR_CPU_PER_KEY, cpuPer);
            this.putAttr(ATTR_MEM_PER_KEY, memPer);
            this.putAttr(ATTR_MAX_LICS_KEY, maxLics);
        }

        public ThirdPartyRegDTO(D_NFV_ThirdPartyModel model) {
            super(ThirdPartyRegistry.class, model);
            this.putAttr(ATTR_CPU_PER_KEY, model.getCpuPer());
            this.putAttr(ATTR_MEM_PER_KEY, model.getMemPer());
            this.putAttr(ATTR_MAX_LICS_KEY, model.getMaxLics());
            this.putAttr(ATTR_AVAIL_LICS_KEY, model.getAvailLics());
        }

        @Override
        public DModel toModel() {
            return new D_NFV_ThirdPartyModel(this);
        }

        String getVendorName() {
            return getParentKey();
        }

        String getFunction() {
            return getName();
        }

        long getCPUPer() {
            return (long) this.getAttr(ATTR_CPU_PER_KEY);
        }

        long getMemPer() {
            return (long) this.getAttr(ATTR_MEM_PER_KEY);
        }

        long getMaxLicenses() {
            return (long) this.getAttr(ATTR_MAX_LICS_KEY);
        }

        long getAvailLicenses() {
            return (long) this.getAttr(ATTR_AVAIL_LICS_KEY);
        }
    }
}

