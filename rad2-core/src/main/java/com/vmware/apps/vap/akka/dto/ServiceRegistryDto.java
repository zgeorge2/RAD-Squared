package com.vmware.apps.vap.akka.dto;

import com.vmware.akka.common.RegistryStateDTO;
import com.vmware.ignite.common.DModel;
import com.vmware.apps.vap.ignite.ServiceRegistry;
import com.vmware.vap.service.dto.EndpointStateEnum;

public class ServiceRegistryDto extends RegistryStateDTO {
    private String serviceName;
    private String vcUUID;
    private String vmMOR;
    private String state;
    private String configuration;
    private int pid;
    private int ppid;
    private String category;
    private String displayName;
    private String command;
    private String vendor;
    private String version;
    private String installPath;
    private boolean started;
    private String startMode;
    private String groupKey;
    private long startTime;
    private String ports;
    private String userName;

    public ServiceRegistryDto(String serviceName, String vmID) {

        super(ServiceRegistry.DService.class, vmID, serviceName);
        this.serviceName = serviceName;
        String[] vmIDParts = vmID.split("_");
        this.vmMOR = vmIDParts[1];
        this.vcUUID = vmIDParts[0];
        this.state = EndpointStateEnum.NOTCONFIGURED.getValue();
    }

    public ServiceRegistryDto(ServiceRegistry.DService model) {

        super(ServiceRegistry.class, model);
        //TODO: i think model to dto conversion can be done using dozer mapping.
        this.serviceName = model.getServiceName();
        this.state = model.getState();
        this.vmMOR = model.getVmMOR();
        this.vcUUID = model.getVcUUID();
        this.configuration = model.getConfiguration();
        this.pid = model.getPid();
        this.ppid = model.getPpid();
        this.category = model.getCategory();
        this.displayName = model.getDisplayName();
        this.command = model.getCommand();
        this.vendor = model.getVendor();
        this.version = model.getVersion();
        this.installPath = model.getInstallPath();
        this.started = model.isStarted();
        this.startMode = model.getStartMode();
        this.groupKey = model.getGroupKey();
        this.startTime = model.getStartTime();
        this.ports = model.getPorts();
        this.userName = model.getUserName();
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getState() {
        return state;
    }

    public String getVmMOR() {
        return vmMOR;
    }

    public String getConfiguration() {
        return configuration;
    }

    public String getVcUUID() {
        return vcUUID;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getPpid() {
        return ppid;
    }

    public void setPpid(int ppid) {
        this.ppid = ppid;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getInstallPath() {
        return installPath;
    }

    public void setInstallPath(String installPath) {
        this.installPath = installPath;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public String getStartMode() {
        return startMode;
    }

    public void setStartMode(String startMode) {
        this.startMode = startMode;
    }

    public String getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getPorts() {
        return ports;
    }

    public void setPorts(String ports) {
        this.ports = ports;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public DModel toModel() {
        return new ServiceRegistry.DService(this);
    }

    @Override
    public String getKey() {
        return String.format("%s_%s", this.vmMOR, this.serviceName);
    }
}
