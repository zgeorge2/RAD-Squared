package com.vmware.apps.ic.ignite;

import com.vmware.akka.common.RegistryStateDTO;
import com.vmware.apps.ic.akka.InvestmentClub;
import com.vmware.ignite.common.BaseModelRegistry;
import com.vmware.ignite.common.DModel;

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
