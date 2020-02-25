/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.ctrl.deps;

import com.rad2.ctrl.ControllerDependency;
import com.rad2.ignite.common.RegistryManager;

import java.util.UUID;

public class UUIDGenerator implements ControllerDependency {
    public UUIDGenerator(RegistryManager rm) {
    }

    public UUID generateNewUUID() {
        return UUID.randomUUID();
    }
}
