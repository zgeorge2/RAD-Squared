/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.ctrl.deps;

import com.rad2.common.serialization.IAkkaSerializable;

public class JobRef implements IJobRef, IAkkaSerializable {
    private String name; // the name of the job
    private String parentKey; // made unique, typically by the local system name

    public JobRef() {
        this(null, null);
    }

    public JobRef(IJobRef ijr) {
        this(ijr.getParentKey(), ijr.getName());
    }

    public JobRef(String parentKey, String name) {
        this.parentKey = parentKey;
        this.name = name;
    }

    @Override
    public String getParentKey() {
        return this.parentKey;
    }

    @Override
    public String getName() {
        return name;
    }

}
