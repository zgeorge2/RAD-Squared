/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.ctrl.deps;

import com.rad2.common.utils.PrintUtils;

/**
 * This class gets autowired into BaseController. it is here only for illustration
 */
public class FakeControllerDependency implements IFakeControllerDependency {
    public void doSomething() {
        PrintUtils.print("*** [%s] is trying to doSomething Fakey ***", this.getType());
    }

    public void doSomethingElse() {
        PrintUtils.print("*** [%s] is trying to doSomethingElse Fakey ***", this.getType());
    }
}

