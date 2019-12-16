package com.vmware.examples.gen1;

import com.vmware.common.utils.PrintUtils;

public class SimpleContainer extends BaseContainer<SimpleObject> {
    public SimpleContainer() {
    }

    public void doSomethingSimple(String message) {
        PrintUtils.printToActor("SIMPLE MESSAGE:%s", message);
    }
}

