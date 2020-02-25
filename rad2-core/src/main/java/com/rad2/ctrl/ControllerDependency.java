/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.ctrl;

/**
 * Controllers may have common dependencies and these are added to BaseController during initialization. For
 * auto-wiring, ensure that all such depdencies implement this interface.
 */
public interface ControllerDependency {
    default String getType() {
        return this.getClass().getSimpleName();
    }
}

