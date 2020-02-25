/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.common.utils;

class RangeCheck {
    private int minInclude;
    private int maxExclude;

    public RangeCheck(int minInclude, int maxExclude) {
        this.minInclude = minInclude;
        this.maxExclude = maxExclude;
    }

    public boolean isInRange(int value) {
        return value >= minInclude && value < maxExclude;
    }
}
