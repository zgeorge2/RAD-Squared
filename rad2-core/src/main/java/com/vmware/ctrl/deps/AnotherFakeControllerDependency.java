package com.vmware.ctrl.deps;

import com.vmware.common.utils.PrintUtils;

/**
 * This class gets autowired into BaseController. it is here only for illustration
 */
public class AnotherFakeControllerDependency implements IFakeControllerDependency {
    public void doSomething() {
        PrintUtils.printToActor("*** [%s] is trying to doSomething Fakey ***", this.getType());
    }

    public void doSomethingElse() {
        PrintUtils.printToActor("*** [%s] is trying to doSomethingElse Fakey ***", this.getType());
    }
}

