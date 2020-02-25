/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.examples.test;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

public class BitOpsTest {

    @Test
    public void testSizeOf() {
        BitOps bops = new BitOps();
        for (int i = 15; i >= -15; i--) {
            Assert.assertEquals("sizeOf should equal",
                    bops.sizeOf(i), Integer.toBinaryString(i).length());
        }
    }

    @Test
    public void testNumOfOneBits() {
        BitOps bops = new BitOps();
        for (int i = 15; i >= -15; i--) {
            Assert.assertEquals("numOfOneBits should equal",
                    bops.numOfOneBits(i),
                    StringUtils.countMatches(Integer.toBinaryString(i), '1'));
        }
    }
}