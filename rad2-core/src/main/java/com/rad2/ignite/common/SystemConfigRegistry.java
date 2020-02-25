/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.ignite.common;

import com.rad2.akka.common.RegistryStateDTO;
import com.rad2.akka.common.SystemProperties;
import com.rad2.ignite.db.IgniteQueries;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The SystemConfigRegistry stores system configurations of each participating system in the cluster.
 * Primarily this includes, the systemName, hostname and port of each node. The registry is used to construct
 * and to look up Actor paths be they local or remote
 */
public class SystemConfigRegistry extends BaseModelRegistry<SystemConfigRegistry.DSystemConfig> {
    private SystemProperties localSysProps;

    @Override
    public void preAdd(RegistryStateDTO dto) {
        this.localSysProps = (SystemProperties) dto;
    }

    @Override
    protected Class getModelClass() {
        return DSystemConfig.class;
    }

    /**
     * Get the ignite queries registered by name in resource files from the SystemProperties instance, which
     * is populated at startup from those files.
     *
     * @return
     */
    public IgniteQueries getIgniteQueries() {
        return this.getLocalSysProps().getIgniteQueries();
    }

    /**
     * Get the value of the property in the resource files at the specified path from the SystemProperties
     * instance, which is populated at startup from those files.
     *
     * @return
     */
    public String getSysProp(String path) {
        return this.getLocalSysProps().get(path);
    }

    /**
     * Get the value of the local system name registered in the resource files in the SystemProperties
     * instance, which is populated at startup from those files.
     *
     * @return
     */
    public final String getLocalSystemName() {
        return this.getLocalSysProps().getSystemName();
    }

    /**
     * Get all the system names of the nodes in the cluster. The returned RegisteredSystems instance has both
     * the local system names and the remote system names.
     *
     * @return
     */
    public RegisteredSystems getAllSystems() {
        String localSysName = this.getLocalSystemName();
        List<String> otherSysNames = this.getAll().stream()
            .map(otherSC -> otherSC.getSystemName())
            .filter(otherSCName -> !isLocalSystem(localSysName, otherSCName))
            .collect(Collectors.toList());
        return new RegisteredSystems(localSysName, otherSysNames);
    }

    /**
     * From the registry, get the fully qualified Akka system path for the local system using the localKey
     * (regId of the ignite entry corresponding to this local system). The localKey is the regId of the local
     * system (in this running node) as registered in Ignite (see DSYSTEMCONFIG table's KEY column). It is
     * created, returned and held at the caller that adds the SystemProperties (RegStateDTO) of the local
     * system to the ignite registry
     *
     * @return
     */
    public String getLocalSystemPath(String localKey) {
        DSystemConfig localSC = this.get(localKey);
        DSystemConfig otherSC = this.get(getKey(localSysProps.getParentKey(), getLocalSystemName()));
        return this.getPathPrefix(localSC, otherSC);
    }

    /**
     * From the registry, get the fully qualified Akka system path for the OTHER (local OR remote) system
     * using the system name of the OTHER (local OR remote) system.  Hence, note that this is a generic method
     * that can be used to find the path of either an Actor in the local system or a remote system. The
     * localKey is also required for this call as it is used to construct a local or remote path depending on
     * the whether the passed in "otherSystemName" is actually a remote system or the name of the local system
     * itself. All system configs, which have the system names and registration ids of each node in the
     * cluster are registered in Ignite (see DSYSTEMCONFIG table's KEY column) on startup.
     *
     * @return
     */
    public final String getPath(String localKey, String otherSystemName, String... pathParts) {
        DSystemConfig localSC = this.get(localKey);
        DSystemConfig otherSC = this.get(getKey(localSysProps.getParentKey(), otherSystemName));
        StringBuffer sb = new StringBuffer();
        sb.append(this.getPathPrefix(localSC, otherSC));
        for (String pp : pathParts) {
            sb.append("/").append(pp);
        }
        return sb.toString();
    }

    private String getPathPrefix(DSystemConfig localSC, DSystemConfig otherSC) {
        return String.format("%s%s%s",
            getProtocolPrefix(localSC, otherSC),
            otherSC.getSystemName(),
            this.getProtocolSuffix(localSC, otherSC));
    }

    private String getProtocolPrefix(DSystemConfig localSC, DSystemConfig otherSC) {
        return (isLocalSystemConfig(localSC, otherSC)) ? "akka://" : "akka.tcp://";
    }

    private String getProtocolSuffix(DSystemConfig localSC, DSystemConfig otherSC) {
        return isLocalSystemConfig(localSC, otherSC) ?
            "/user" : (String.format("@%s:%s/user", otherSC.getHostname(), otherSC.getPort()));
    }

    private boolean isLocalSystemConfig(DSystemConfig localSC, DSystemConfig otherSC) {
        return localSC.equals(otherSC);
    }

    private boolean isLocalSystem(String localSCName, String otherSCName) {
        return localSCName.equals(otherSCName);
    }

    private SystemProperties getLocalSysProps() {
        return localSysProps;
    }

    /**
     * DTOs for this class
     */
    public static class RegisteredSystems {
        private String localSystem;
        private List<String> remoteSystems;

        public RegisteredSystems(String localSystem, List<String> remoteSystems) {
            this.remoteSystems = remoteSystems;
            this.localSystem = localSystem;
        }

        public List<String> getRemoteSystems() {
            return this.remoteSystems;
        }

        public String getLocalSystem() {
            return this.localSystem;
        }
    }

    /**
     * Registry model class
     */
    public static class DSystemConfig extends DModel {
        @QuerySqlField
        private String hostname;
        @QuerySqlField
        private String port;

        public DSystemConfig(SystemProperties dto) {
            super(dto);
            this.hostname = dto.getHostname();
            this.port = dto.getPort();
        }

        @Override
        public RegistryStateDTO toRegistryStateDTO() {
            return new SystemProperties(this);
        }

        public String getHostname() {
            return hostname;
        }

        public String getPort() {
            return port;
        }

        public String getSystemName() {
            return this.getName();
        }

        @Override
        public boolean equals(Object obj) {
            return this.getSystemName().equalsIgnoreCase(((DSystemConfig) obj).getSystemName());
        }
    }
}

