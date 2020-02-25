/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.examples.gen1;

import com.rad2.common.utils.PrintUtils;

public class SimpleContainer extends BaseContainer<SimpleObject> {
    public SimpleContainer() {
    }

    public void doSomethingSimple(String message) {
        PrintUtils.printToActor("SIMPLE MESSAGE:%s", message);
    }
}

