/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.sb.app;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class SBApplicationCmdLineRunner implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) {
        // nothing to do for now
    }
}
