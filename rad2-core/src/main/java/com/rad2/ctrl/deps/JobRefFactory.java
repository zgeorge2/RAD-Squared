package com.rad2.ctrl.deps;

import com.rad2.ctrl.ControllerDependency;
import com.rad2.ignite.common.RegistryManager;

import java.util.UUID;

/**
 * This class gets autowired into BaseController. It is used to create IJobRef instances
 */
public class JobRefFactory implements ControllerDependency {
    private RegistryManager rm;

    public JobRefFactory(RegistryManager rm) {
        this.rm = rm;
    }

    public IJobRef create() {
        String name = "JT_" + UUID.randomUUID();
        return new JobRef(rm.getAU().getLocalSystemName(), name);
    }

    public IJobRef create(String parentKey, String name) {
        return new JobRef(parentKey, name);
    }
}

