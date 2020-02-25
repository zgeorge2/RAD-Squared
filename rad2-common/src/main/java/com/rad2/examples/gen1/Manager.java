/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.examples.gen1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Manager<K extends DObject, T extends BaseContainer> {
    private Map<String, T> containerList;

    public Manager(List<T> regList) {
        this.containerList = new HashMap<>();
        regList.stream().forEach(c -> {
            c.initialize(this);
            this.containerList.put(c.getClass().getSimpleName(), c);
        });
    }

    public static void main(String[] args) {
        List<BaseContainer<? extends DObject>> regList = new ArrayList<>();
        regList.add(new SimpleContainer());
        regList.add(new ComplexContainer());
    }

    public T get(Class<T> regClass) {
        return this.containerList.get(regClass.getSimpleName());
    }
}