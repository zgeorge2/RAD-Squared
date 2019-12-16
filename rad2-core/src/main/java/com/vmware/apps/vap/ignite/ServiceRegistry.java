package com.vmware.apps.vap.ignite;

import com.vmware.apps.vap.akka.dto.ServiceRegistryDto;
import com.vmware.ignite.common.BaseModelRegistry;
import com.vmware.ignite.common.DModel;
import com.vmware.vap.service.VapServiceUtils;
import com.vmware.vap.service.dto.EndpointStateEnum;
import com.vmware.vap.service.dto.PluginInfo;
import com.vmware.vap.service.exception.RegistryException;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.vmware.common.constants.VapServiceConstants.*;

public class ServiceRegistry
    extends BaseModelRegistry<ServiceRegistry.DService> {
    @Override
    protected Class getModelClass() {
        return DService.class;
    }

    public List<DService> getServicesByVm(String vcUUID, String vcMor) {
        HashMap<String, String> fields = new HashMap<>();
        fields.put(VC_UUID, vcUUID);
        fields.put(VM_MOR, vcMor);
        return this.getResultFromQueryWithFields(fields);
    }

    public DService getServiceForVm(String vmID, String serviceName) {
        HashMap<String, String> fields = new HashMap<>();
        String[] vmIDParts = vmID.split("_");
        fields.put(VC_UUID, vmIDParts[0]);
        fields.put(VM_MOR, vmIDParts[1]);
        fields.put(SERVICENAME, serviceName);
        List<DService> resultFromQueryWithFields = this
            .getResultFromQueryWithFields(fields);
        return resultFromQueryWithFields.size() > 0 ?
            resultFromQueryWithFields.get(0) :
            null;
    }

    public DService updateServiceState(String key, EndpointStateEnum status)
        throws RegistryException {
        try {
            DService svc = this.get(key);
            if (svc == null) {
                throw new RegistryException(String
                  .format("Could not find service with key %s", key));
            }
            this.update(svc.updateState(status.getValue()));
            return svc;
        } catch (Exception ex) {
            throw new RegistryException(ex.getMessage(), ex);
        }
    }

    public DService updateServiceConfig(String key, PluginInfo pluginInfo)
        throws RegistryException {
        DService svc = this.get(key);
        if (svc == null) {
            throw new RegistryException(String
                .format("Could not find service with key %s", key));
        }

        if (MapUtils.isEmpty(pluginInfo.getPlugin_config())) {
            return svc;
        }
        String pluginJsonStr = generateConfigJsonString(pluginInfo);
        if (StringUtils.isNotBlank(pluginJsonStr)) {
            if (!checkIfConfigsAreEqual(pluginJsonStr, svc.configuration)) {
                this.update(svc.updateConfiguration(pluginJsonStr));
            }
        }

        return svc;
    }

    public DService updateServiceConfigWithKeys(String key,
                                                Map<String, String> configMap) throws RegistryException {
        DService svc = this.get(key);
        if (svc == null) {
            throw new RegistryException(String
                .format("Could not find service with key %s", key));
        }

        if (MapUtils.isEmpty(configMap)) {
            return svc;
        }

        PluginInfo pluginInfoFromReg = VapServiceUtils
            .convertToObject(svc.configuration, PluginInfo.class);

        if (updatePluginConfig(configMap, pluginInfoFromReg)) {
            String pluginJsonStr = VapServiceUtils
                .convertToJson(pluginInfoFromReg, PluginInfo.class);
            this.update(svc.updateConfiguration(pluginJsonStr));
        }

        return svc;
    }

    private ServiceRegistry getServiceRegistry() {
        return this.reg(ServiceRegistry.class);
    }

    private String generateConfigJsonString(PluginInfo pluginInfo) {

        if (MapUtils.isEmpty(pluginInfo.getPlugin_identifier())) {
            pluginInfo.setPlugin_identifier(MapUtils.EMPTY_MAP);
        }

        Map<String, String> plugin_config = new HashMap<>(pluginInfo
          .getPlugin_config());

        PluginInfo plugin = new PluginInfo(plugin_config,
          pluginInfo.getPlugin_identifier());

        plugin.getPlugin_config().remove("password");

        return VapServiceUtils.convertToJson(plugin, PluginInfo.class);
    }

    private boolean checkIfConfigsAreEqual(String configFromReq,
                                           String configFromRegistry) {

        PluginInfo pluginFromRequest = VapServiceUtils
            .convertToObject(configFromReq, PluginInfo.class);
        PluginInfo pluginFromRegistry = VapServiceUtils
            .convertToObject(configFromRegistry, PluginInfo.class);

        if (pluginFromRegistry == null || !pluginFromRequest.getPlugin_config()
            .equals(pluginFromRegistry.getPlugin_config())) {
            return false;
        }

        if (MapUtils.isNotEmpty(pluginFromRequest.getPlugin_identifier())
            && !pluginFromRequest.getPlugin_identifier()
            .equals(pluginFromRegistry.getPlugin_identifier())) {
            return false;
        }

        return true;
    }

    private boolean updatePluginConfig(Map<String, String> configMap,
                                       PluginInfo pluginInfo) {

        boolean isModified = false;

        Map<String, String> plugin_config = pluginInfo.getPlugin_config();
        Map<String, String> plugin_identifier = pluginInfo
            .getPlugin_identifier();

        for (Map.Entry<String, String> configEntry : configMap.entrySet()) {

            if (plugin_identifier.containsKey(configEntry.getKey())) {
                if (!configEntry.getValue().equalsIgnoreCase(plugin_identifier
                    .get(configEntry.getKey()))) {
                    plugin_identifier
                        .put(configEntry.getKey(), configEntry.getValue());
                    isModified = true;
                }
            } else if (plugin_config.containsKey(configEntry.getKey())) {
                if (!configEntry.getValue()
                    .equalsIgnoreCase(plugin_config.get(configEntry.getKey()))) {
                    plugin_config
                        .put(configEntry.getKey(), configEntry.getValue());
                    isModified = true;
                }
            } else {
                plugin_config.put(configEntry.getKey(), configEntry.getValue());
                isModified = true;
            }
        }

        return isModified;
    }

    public static class DService extends DModel {
        @QuerySqlField
        private String serviceName;
        @QuerySqlField
        private String state;
        @QuerySqlField
        private String vcUUID;
        @QuerySqlField
        private String vmMOR;
        @QuerySqlField
        private String configuration;
        @QuerySqlField
        private long lastMsgRcvdAt;
        @QuerySqlField
        private int pid;
        @QuerySqlField
        private int ppid;
        @QuerySqlField
        private String category;
        @QuerySqlField
        private String displayName;
        @QuerySqlField
        private String command;
        @QuerySqlField
        private String vendor;
        @QuerySqlField
        private String version;
        @QuerySqlField
        private String installPath;
        @QuerySqlField
        private boolean started;
        @QuerySqlField
        private String startMode;
        @QuerySqlField
        private String groupKey;
        @QuerySqlField
        private long startTime;
        @QuerySqlField
        private String ports;
        @QuerySqlField
        private String userName;

        public DService(ServiceRegistryDto dto) {
            super(dto);
            this.serviceName = dto.getServiceName();
            this.state = dto.getState();
            this.vcUUID = dto.getVcUUID();
            this.vmMOR = dto.getVmMOR();
            this.configuration = dto.getConfiguration();
            this.pid = dto.getPid();
            this.ppid = dto.getPpid();
            this.category = dto.getCategory();
            this.displayName = dto.getDisplayName();
            this.command = dto.getCommand();
            this.vendor = dto.getVendor();
            this.version = dto.getVersion();
            this.installPath = dto.getInstallPath();
            this.started = dto.isStarted();
            this.startMode = dto.getStartMode();
            this.groupKey = dto.getGroupKey();
            this.startTime = dto.getStartTime();
            this.ports = dto.getPorts();
            this.userName = dto.getUserName();
        }

        @Override
        public ServiceRegistryDto toRegistryStateDTO() {
            return new ServiceRegistryDto(this);
        }

        public ServiceRegistryDto updateState(String state) {
            this.state = state;
            return this.toRegistryStateDTO();
        }

        public ServiceRegistryDto updateConfiguration(
            String pluginJsonStr) {

            if (StringUtils.isNotBlank(pluginJsonStr)) {
                this.configuration = pluginJsonStr;
            }

            return this.toRegistryStateDTO();
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

        public long getLastMsgRcvdAt() {
            return lastMsgRcvdAt;
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
    }
}
