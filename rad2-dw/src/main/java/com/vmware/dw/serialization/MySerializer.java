package com.vmware.dw.serialization;

import akka.serialization.SerializerWithStringManifest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.common.serialization.IAkkaSerializable;

import java.io.IOException;
import java.io.NotSerializableException;

public class MySerializer extends SerializerWithStringManifest {
    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String manifest(Object o) {
        if (o instanceof IAkkaSerializable) {
            return ((IAkkaSerializable) o).manifest();
        }
        return null;
    }

    @Override
    public Object fromBinary(byte[] bytes, String manifest) throws NotSerializableException {
        Object ret = null;
        try {
            ret = objectMapper.readValue(bytes, Class.forName(manifest));
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
            ret = objectMapper.writeValueAsBytes(o);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return ret;
    }
}
