/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.ctrl.deps;

import com.rad2.ctrl.ControllerDependency;

/**
 * This  interface is for testing only
 */
public interface IFakeControllerDependency extends ControllerDependency {
    void doSomething();

    void doSomethingElse();
}

