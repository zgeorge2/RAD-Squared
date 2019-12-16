package com.vmware.vap.service;

import com.google.gson.Gson;
import com.vmware.common.constants.VapServiceConstants;

import java.lang.reflect.Type;

public class VapServiceUtils {

    public static String buildQuestionString(int n) {
        if (n <= 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = 0; i < n; i++) {
            if(i > 0){
                sb.append(",");
            }
            sb.append("?");
        }
        sb.append(")");
        return sb.toString();
    }

    public static final String[] splitString(String data, String regex){
        return data.split(regex);
    }

    public static final String contructEndpointID(final String vcId, String vmId){
        return vcId + VapServiceConstants.ENDPOINT_DELIMITER + vmId;
    }

    public static final String[] getEndpointID(final String id){
        return splitString(id, VapServiceConstants.ENDPOINT_DELIMITER);
    }

    public static String convertToJson(Object src, Type type){
        Gson gson = new Gson();
        return gson.toJson(src, type);
    }

    public static <T> T convertToObject(String json, Type type){
        Gson gson = new Gson();
        return gson.fromJson(json, type);
    }

}
