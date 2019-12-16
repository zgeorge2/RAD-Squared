package com.vmware.akka.common;

import com.vmware.ignite.common.BaseModelRegistry;
import com.vmware.ignite.common.DModel;
import com.vmware.ignite.common.RegistryManager;
import com.vmware.ignite.common.UsesRegistryManager;

/**
 * In addition to BaseActor characteristics, this class also has State maintained in the Registry
 */
public abstract class BaseActorWithRegState extends BaseActor implements UsesRegistryManager {
    private String regId;
    private Class regClassForState;

    protected BaseActorWithRegState(RegistryManager rm, RegistryStateDTO dto) {
        super(rm);
        dto.putAttr(RegistryStateDTO.ATTR_ACTOR_PATH_KEY, this.self().path().toString());
        this.regClassForState = dto.getRegistryClassForState();
        // initialize the reg id of the state stored in the registry.
        this.regId = getReg().add(dto);
    }

    protected BaseModelRegistry getReg() {
        return reg(this.regClassForState);
    }

    protected DModel getModel() {
        return this.getReg().get(this.getRegId());
    }

    protected String getParentKey() {
        return getModel().getParentKey();
    }

    protected String getName() {
        return getModel().getName();
    }

    protected String getKey() {
        return getModel().getKey();
    }

    public String getRegId() {
        return this.regId;
    }
}

