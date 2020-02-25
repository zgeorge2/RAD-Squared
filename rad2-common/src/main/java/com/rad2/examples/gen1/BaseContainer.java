/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.examples.gen1;

import com.rad2.common.utils.PrintUtils;

public abstract class BaseContainer<K extends DObject> implements UsesManager {
    private Manager<K, ? extends BaseContainer<K>> rm;

    public BaseContainer() {
    }

    public <T extends BaseContainer<K>> void initialize(Manager<K, T> rm) {
        this.rm = rm;
        PrintUtils.printToActor("Initializing %s", this.getClass().getSimpleName());
    }

    public Manager<K, ? extends BaseContainer<K>> getManager() {
        return this.rm;
    }

    /**
     * simple broadcaster across the compute grid
     *
     * @return
     */
    public void doSomething(String message) {
        PrintUtils.printToActor("MESSAGE:%s", message);
    }
}

