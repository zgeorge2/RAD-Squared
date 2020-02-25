/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.examples.gen1;

public class ComplexObject extends DObject {
    private String complexPlace;

    public ComplexObject(String name, String place, String complexPlace) {
        super(name, place);
        this.complexPlace = complexPlace;
    }

    public String getComplexPlace() {
        return complexPlace;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("COMPLEX OBJ STATE[%s] [NAME:%s] [PLACE:%s] [COMPLEX PLACE: %s]\n",
            this.getClass().getSimpleName(), this.getName(), this.getPlace(), this.getComplexPlace()));
        return sb.toString();
    }
}
