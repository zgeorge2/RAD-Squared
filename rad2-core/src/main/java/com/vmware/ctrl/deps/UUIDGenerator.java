package com.vmware.ctrl.deps;

import com.vmware.ctrl.ControllerDependency;
import com.vmware.ignite.common.RegistryManager;

import java.util.UUID;

public class UUIDGenerator implements ControllerDependency {
    public UUIDGenerator(RegistryManager rm) {
    }

    public UUID generateNewUUID() {
        return UUID.randomUUID();
    }
}
