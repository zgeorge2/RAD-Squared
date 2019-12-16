package com.vmware.akka.common;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigList;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import com.vmware.ignite.common.DModel;
import com.vmware.ignite.common.SystemConfigRegistry;
import com.vmware.ignite.db.IgniteQueries;

import java.util.List;
import java.util.stream.Collectors;

public class SystemProperties extends RegistryStateDTO {
    public static final String PROJECT_PROP = "vapaas";
    public static final String SYSTEM_PROP = PROJECT_PROP + "." + "system";
    public static final String PORT_PROP = PROJECT_PROP + "." + "port";
    public static final String HOSTNAME_PROP = PROJECT_PROP + "." + "hostname";
    private static final String IGNITE_QUERIES_PROP = "ignite.queries";
    private static final String IGNITE_QUERY_NAME_PROP = "name";
    private static final String IGNITE_QUERY_TEMPLATE_PROP = "template";
    private static final String IGNITE_QUERY_MODEL_PROP = "model";
    private static final String IGNITE_MACHINE_ADDRESS_PROP = "ignite.cluster.machines";
    private static String DISCOVERY_MODE_PROP = "ignite.cluster.discovery.mode";
    private Config config;
    private IgniteQueries igniteQueries;

    public SystemProperties(Config config) {
        super(SystemConfigRegistry.class, null, config.getString(SYSTEM_PROP));
        this.config = config;
        this.igniteQueries = this.createIgniteQueries();
    }

    public SystemProperties(SystemConfigRegistry.DSystemConfig model) {
        super(SystemConfigRegistry.class, null, model.getSystemName());
        this.config = null;
    }

    public IgniteQueries getIgniteQueries() {
        return igniteQueries;
    }

    public String getSystemId() {
        return String.format("%s@%s_%s", this.getSystemName(), this.getHostname(), this.getPort());
    }

    public String getHostname() {
        return this.get(HOSTNAME_PROP);
    }

    public String getPort() {
        return this.get(PORT_PROP);
    }

    public String getSystemName() {
        return this.getName();
    }

    public String get(String path) {
        return this.config.getString(path);
    }

    public List<String> getIgniteMachines() {
        return this.config.getList(IGNITE_MACHINE_ADDRESS_PROP).stream()
            .map(configValue -> (String) configValue.unwrapped())
            .collect(Collectors.toList());
    }

    public String getDiscoveryMode() { return this.config.getString(DISCOVERY_MODE_PROP); }

    public Config getRawConfig() {
        return this.config;
    }

    @Override
    public DModel toModel() {
        return new SystemConfigRegistry.DSystemConfig(this);
    }

    private IgniteQueries createIgniteQueries() {
        IgniteQueries iq = new IgniteQueries();
        ConfigList cl = getRawConfig().getList(IGNITE_QUERIES_PROP);
        for (ConfigValue cv : cl) {
            Config c = ((ConfigObject) cv).toConfig();
            iq.addQuery(
                c.getString(IGNITE_QUERY_MODEL_PROP),
                c.getString(IGNITE_QUERY_NAME_PROP),
                c.getString(IGNITE_QUERY_TEMPLATE_PROP));
        }
        return iq;
    }
}
