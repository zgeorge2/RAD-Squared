/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.akka.serialization;

import akka.serialization.SerializerWithStringManifest;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rad2.common.serialization.IAkkaSerializable;

import java.io.IOException;

public class MySerializer extends SerializerWithStringManifest {
    private final ObjectMapper mapper;

    public MySerializer() {
        this.mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
    }

    @Override
    public String manifest(Object o) {
        if (o instanceof IAkkaSerializable) {
            return ((IAkkaSerializable) o).manifest();
        }
        return null;
    }

    @Override
    public Object fromBinary(byte[] bytes, String manifest) {
        Object ret = null;
        try {
            ret = mapper.readValue(bytes, Class.forName(manifest));
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }

        return ret;
    }

    @Override
    public int identifier() {
        return 12345678;
    }

    @Override
    public byte[] toBinary(Object o) {
        byte[] ret = null;
        try {
            ret = mapper.writeValueAsBytes(o);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return ret;
    }
}
