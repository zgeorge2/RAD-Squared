package com.vmware.apps.bank.ignite;

import com.vmware.akka.common.RegistryStateDTO;
import com.vmware.apps.bank.akka.Bank;
import com.vmware.ignite.common.BaseModelRegistry;
import com.vmware.ignite.common.DModel;

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
