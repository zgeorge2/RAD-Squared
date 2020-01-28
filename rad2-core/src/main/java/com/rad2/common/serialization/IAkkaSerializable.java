package com.rad2.common.serialization;

import java.io.Serializable;

public interface IAkkaSerializable extends Serializable {
    /**
     * return the class of the implementing class
     */
    default String manifest() {
        return this.getClass().getName();
    }
}
