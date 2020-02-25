/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.examples.gen1;

public interface UsesManager {
    <K extends DObject> Manager<K, ? extends BaseContainer<K>> getManager();

    default <T extends BaseContainer> T reg(Class tClass) {
        return (T) getManager().get(tClass);
    }
}

