package com.vmware.vap.service.dto;


public enum CloudAccountType {
    VCENTER("vcenter");

    private String value;

    CloudAccountType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        return value;
    }
}
