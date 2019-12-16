package com.vmware.apps.vap.akka.dto;

import com.vmware.akka.common.RegistryStateDTO;
import com.vmware.ignite.common.DModel;
import com.vmware.apps.vap.ignite.RDCStateRegistry;

public class RDCStateRegistryDto extends RegistryStateDTO {

    private String rdcID;

    private String rdcIP;

    private Integer rdcHealthStatus;

    private String rdcComponentHealth;

    private String lastUpdatedTime;


    public RDCStateRegistryDto(String rdcID, String rdcIP) {
        super(RDCStateRegistry.RDCState.class, null, rdcID);
        this.rdcID = rdcID;
        this.rdcIP = rdcIP;
    }

    public RDCStateRegistryDto(RDCStateRegistry.RDCState model) {
        super(RDCStateRegistry.RDCState.class, model);
        this.rdcID = model.getRdcID();
        this.rdcIP = model.getRdcIP();
        this.rdcHealthStatus = model.getRdcHealthStatus();
        this.rdcComponentHealth = model.getRdcComponentHealth();
        this.lastUpdatedTime = model.getLastUpdatedTime();

    }

    public String getRdcID() {
        return rdcID;
    }


    public void setRdcID(String rDCID) {
        rdcID = rDCID;
    }


    public String getRdcIP() {
        return rdcIP;
    }


    public void setRdcIP(String rDCIP) {
        rdcIP = rDCIP;
    }


    public Integer getRdcHealthStatus() {
        return rdcHealthStatus;
    }


    public void setRdcHealthStatus(Integer rDCHealthStatus) {
        rdcHealthStatus = rDCHealthStatus;
    }


    public String getRdcComponentHealth() {
        return rdcComponentHealth;
    }


    public void setRdcComponentHealth(String rDCComponentHealth) {
        rdcComponentHealth = rDCComponentHealth;
    }


    public String getLastUpdatedTime() {
        return lastUpdatedTime;
    }


    public void setLastUpdatedTime(String lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }


    @Override
    public <K extends DModel> K toModel() {
        return (K) new RDCStateRegistry.RDCState(this);
    }


    @Override
    public String getKey() {
        return this.rdcID;
    }
}
