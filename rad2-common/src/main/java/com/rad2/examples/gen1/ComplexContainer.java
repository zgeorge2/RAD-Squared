/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.examples.gen1;

import com.rad2.common.utils.PrintUtils;

public class ComplexContainer extends BaseContainer<ComplexObject> {
    public ComplexContainer() {

    }

    public void doSomethingComplex(String message) {
        PrintUtils.printToActor("COMPLEX MESSAGE:%s", message);
    }
}

