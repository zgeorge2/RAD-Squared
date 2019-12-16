package com.vmware.examples.gen1;

import com.vmware.common.utils.PrintUtils;

public class ComplexContainer extends BaseContainer<ComplexObject> {
    public ComplexContainer() {

    }

    public void doSomethingComplex(String message) {
        PrintUtils.printToActor("COMPLEX MESSAGE:%s", message);
    }
}

