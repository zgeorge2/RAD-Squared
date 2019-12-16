package com.vmware.apps.ic.ignite;

import com.vmware.akka.common.RegistryStateDTO;
import com.vmware.apps.ic.akka.Member;
import com.vmware.ignite.common.BaseModelRegistry;
import com.vmware.ignite.common.DModel;

public class MemberRegistry extends BaseModelRegistry<MemberRegistry.DMemberModel> {
    @Override
    protected Class getModelClass() {
        return DMemberModel.class;
    }

    @Override
    public Class getParentRegistryClass() {
        return InvestmentClubRegistry.class;
    }

    public static class DMemberModel extends DModel {
        public DMemberModel(MemberRegistryDTO dto) {
            super(dto);
        }

        @Override
        public RegistryStateDTO toRegistryStateDTO() {
            return new MemberRegistryDTO(this);
        }

        @Override
        public Class getActorClass() {
            return Member.class;
        }
    }

    public static class MemberRegistryDTO extends RegistryStateDTO {
        public MemberRegistryDTO(String parentKey, String name) {
            super(MemberRegistry.class, parentKey, name);
        }

        public MemberRegistryDTO(DMemberModel model) {
            super(MemberRegistry.class, model);
        }

        @Override
        public DModel toModel() {
            return new DMemberModel(this);
        }
    }
}
