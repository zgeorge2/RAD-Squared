/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.apps.nfv.ignite;

import com.rad2.akka.common.RegistryStateDTO;
import com.rad2.ignite.common.BaseModelRegistry;
import com.rad2.ignite.common.DModel;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

public class ResRegistry extends BaseModelRegistry<ResRegistry.D_NFV_ResModel> {
    @Override
    protected Class getModelClass() {
        return D_NFV_ResModel.class;
    }

    @Override
    public Class getParentRegistryClass() {
        return OrderAcceptorRegistry.class;
    }

    public D_NFV_ResModel reserveResource(String key, long cpu, long mem) {
        return this.apply(key, cc -> cc.reserveResource(cpu, mem));
    }

    public D_NFV_ResModel returnResource(String key, long cpu, long mem) {
        return this.apply(key, cc -> cc.returnResource(cpu, mem));
    }

    public void resetResource(String parentKey) {
        this.applyToChildrenOfParent(parentKey, D_NFV_ResModel::resetResource);
    }

    /**
     * Model class for Data grid
     */
    public static class D_NFV_ResModel extends DModel {
        @QuerySqlField
        private long numCPU;
        @QuerySqlField
        private long availCPU;
        @QuerySqlField
        private long maxMemory;
        @QuerySqlField
        private long availMemory;

        public D_NFV_ResModel(ResRegDTO dto) {
            super(dto);
            this.numCPU = dto.getNumCPU();
            this.availCPU = this.numCPU;
            this.maxMemory = dto.getMaxMemory();
            this.availMemory = this.maxMemory;
        }

        @Override
        public RegistryStateDTO toRegistryStateDTO() {
            return new ResRegDTO(this);
        }

        @Override
        public String toString() {
            return String.format("%30s %15d %15d %15d %15d", getKey(), getNumCPU(), getAvailCPU(),
                getMaxMemory(), getAvailMemory());
        }

        public long getNumCPU() {
            return this.numCPU;
        }

        public long getAvailCPU() {
            return this.availCPU;
        }

        public long getMaxMemory() {
            return maxMemory;
        }

        public long getAvailMemory() {
            return availMemory;
        }

        public D_NFV_ResModel reserveResource(long cpu, long mem) {
            this.availCPU = Math.max(this.availCPU - cpu, 0);
            this.availMemory = Math.max(this.availMemory - mem, 0);
            return this;
        }

        public D_NFV_ResModel returnResource(long cpu, long mem) {
            this.availCPU = Math.min(this.availCPU + cpu, this.getNumCPU());
            this.availMemory = Math.min(this.availMemory + mem, this.getMaxMemory());
            return this;
        }

        public D_NFV_ResModel resetResource() {
            this.availCPU = this.getNumCPU();
            this.availMemory = this.getMaxMemory();
            return this;
        }
    }

    public static class ResRegDTO extends RegistryStateDTO {
        public static final String ATTR_NUM_CPU_KEY = "NUM_CPU_KEY";
        public static final String ATTR_AVAIL_CPU_KEY = "AVAIL_CPU_KEY";
        public static final String ATTR_MAX_MEM_KEY = "MAX_MEM_KEY";
        public static final String ATTR_AVAIL_MEM_KEY = "AVAIL_MEM_KEY";

        public ResRegDTO(String parentKey, String name, long numCPU, long maxMem) {
            super(ResRegistry.class, parentKey, name);
            this.putAttr(ATTR_NUM_CPU_KEY, numCPU);
            this.putAttr(ATTR_MAX_MEM_KEY, maxMem);
        }

        public ResRegDTO(D_NFV_ResModel model) {
            super(ResRegistry.class, model);
            this.putAttr(ATTR_NUM_CPU_KEY, model.getNumCPU());
            this.putAttr(ATTR_AVAIL_CPU_KEY, model.getAvailCPU());
            this.putAttr(ATTR_MAX_MEM_KEY, model.getMaxMemory());
            this.putAttr(ATTR_AVAIL_MEM_KEY, model.getAvailMemory());
        }

        @Override
        public DModel toModel() {
            return new D_NFV_ResModel(this);
        }

        long getNumCPU() {
            return (long) this.getAttr(ATTR_NUM_CPU_KEY);
        }

        long getAvailCPU() {
            return (long) this.getAttr(ATTR_AVAIL_CPU_KEY);
        }

        long getMaxMemory() {
            return (long) this.getAttr(ATTR_MAX_MEM_KEY);
        }

        long getAvailMemory() {
            return (long) this.getAttr(ATTR_AVAIL_MEM_KEY);
        }
    }
}

