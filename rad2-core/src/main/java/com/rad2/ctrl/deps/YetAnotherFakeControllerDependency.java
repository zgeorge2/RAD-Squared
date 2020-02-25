/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.ctrl.deps;

import com.rad2.common.utils.PrintUtils;
import com.rad2.ignite.common.RegistryManager;

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

