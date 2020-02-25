/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.examples.gen1;

import java.util.ArrayList;
import java.util.List;

public class Tester implements UsesManager {
    private Manager<? extends DObject, BaseContainer<? extends DObject>> rm;

    public Tester() {
        List<BaseContainer<? extends DObject>> regList = new ArrayList<>();
        regList.add(new SimpleContainer());
        regList.add(new ComplexContainer());
        this.rm = new Manager<>(regList);
    }

    public static void main(String[] args) {
        Tester t = new Tester();
        SimpleContainer sc = t.reg(SimpleContainer.class);
        ComplexContainer cc = t.reg(ComplexContainer.class);

        sc.doSomethingSimple("Something simple");
        cc.doSomethingComplex("Something complex");
    }

    @Override
    public Manager<? extends DObject, BaseContainer<? extends DObject>> getManager() {
        return rm;
    }
}