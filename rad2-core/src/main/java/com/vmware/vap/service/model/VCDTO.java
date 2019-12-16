package com.vmware.vap.service.model;

/**
 * Representation of a VC Resource instance
 *
 */
public class VCDTO {

    private String vcName;

    private String vcUUID;

    public VCDTO(String vcName, String vcUUID) {
        this.vcName = vcName;
        this.vcUUID = vcUUID;
    }

    public String getVcName() {
        return vcName;
    }

    public String getVcUUID() {
        return vcUUID;
    }

}
