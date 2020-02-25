/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.apps.bank.ignite;

import com.rad2.akka.common.RegistryStateDTO;
import com.rad2.apps.bank.akka.Bank;
import com.rad2.ignite.common.BaseModelRegistry;
import com.rad2.ignite.common.DModel;

public class BankRegistry extends BaseModelRegistry<BankRegistry.DBank> {
    @Override
    protected Class getModelClass() {
        return DBank.class;
    }

    public static class DBank extends DModel {
        public DBank(BankRegistryStateDTO dto) {
            super(dto);
        }

        @Override
        public RegistryStateDTO toRegistryStateDTO() {
            return new BankRegistryStateDTO(this);
        }

        @Override
        public Class getActorClass() {
            return Bank.class;
        }
    }

    public static class BankRegistryStateDTO extends RegistryStateDTO {
        public BankRegistryStateDTO(String parentKey, String name) {
            super(BankRegistry.class, parentKey, name);
        }

        public BankRegistryStateDTO(DBank model) {
            super(BankRegistry.class, model);
        }

        @Override
        public DModel toModel() {
            return new DBank(this);
        }
    }
}
