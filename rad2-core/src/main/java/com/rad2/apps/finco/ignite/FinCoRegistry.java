/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.apps.finco.ignite;

import com.rad2.akka.common.RegistryStateDTO;
import com.rad2.ignite.common.BaseModelRegistry;
import com.rad2.ignite.common.DModel;

public class FinCoRegistry extends BaseModelRegistry<FinCoRegistry.D_FC_FinCo> {
    @Override
    protected Class getModelClass() {
        return D_FC_FinCo.class;
    }

    public static class D_FC_FinCo extends DModel {
        public D_FC_FinCo(FinCoRegistryStateDTO dto) {
            super(dto);
        }

        @Override
        public RegistryStateDTO toRegistryStateDTO() {
            return new FinCoRegistryStateDTO(this);
        }
    }

    public static class FinCoRegistryStateDTO extends RegistryStateDTO {
        public FinCoRegistryStateDTO(String parentKey, String name) {
            super(FinCoRegistry.class, parentKey, name);
        }

        public FinCoRegistryStateDTO(D_FC_FinCo model) {
            super(FinCoRegistry.class, model);
        }

        @Override
        public DModel toModel() {
            return new D_FC_FinCo(this);
        }

        public String getFinCo() {
            return this.getParentKey();
        }

        public String getBranch() {
            return this.getName();
        }
    }
}
