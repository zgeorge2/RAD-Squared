/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.apps.ic.ignite;

import com.rad2.akka.common.RegistryStateDTO;
import com.rad2.apps.ic.akka.InvestmentClub;
import com.rad2.ignite.common.BaseModelRegistry;
import com.rad2.ignite.common.DModel;

public class InvestmentClubRegistry extends BaseModelRegistry<InvestmentClubRegistry.DInvestmentClubModel> {
    @Override
    protected Class getModelClass() {
        return DInvestmentClubModel.class;
    }

    public static class DInvestmentClubModel extends DModel {
        public DInvestmentClubModel(InvestmentClubRegistryDTO dto) {
            super(dto);
        }

        @Override
        public RegistryStateDTO toRegistryStateDTO() {
            return new InvestmentClubRegistryDTO(this);
        }

        @Override
        public Class getActorClass() {
            return InvestmentClub.class;
        }
    }

    public static class InvestmentClubRegistryDTO extends RegistryStateDTO {
        public InvestmentClubRegistryDTO(String parentKey, String name) {
            super(InvestmentClubRegistry.class, parentKey, name);
        }

        public InvestmentClubRegistryDTO(DInvestmentClubModel model) {
            super(InvestmentClubRegistry.class, model);
        }

        @Override
        public DModel toModel() {
            return new DInvestmentClubModel(this);
        }
    }
}
