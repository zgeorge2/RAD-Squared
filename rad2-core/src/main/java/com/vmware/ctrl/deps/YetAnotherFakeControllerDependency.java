package com.vmware.ctrl.deps;

import com.vmware.common.utils.PrintUtils;
import com.vmware.ignite.common.RegistryManager;

/**
 * This class gets autowired into BaseController. it is here only for illustration
 */
public class YetAnotherFakeControllerDependency implements IFakeControllerDependency {
    private RegistryManager rm;

    public YetAnotherFakeControllerDependency(RegistryManager rm) {
        this.rm = rm;
    }

    public void doSomething() {
        PrintUtils.printToActor("*** [%s] is trying to doSomething Fakey ***", this.getType());
    }

    public void doSomethingElse() {
        PrintUtils.printToActor("*** [%s] is trying to doSomethingElse Fakey ***", this.getType());
    }
}

