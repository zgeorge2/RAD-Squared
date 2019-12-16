package com.vmware.apps.vap.ignite;

import com.vmware.akka.common.RegistryStateDTO;
import com.vmware.apps.vap.akka.dto.RDCStateRegistryDto;
import com.vmware.ignite.common.BaseModelRegistry;
import com.vmware.ignite.common.DModel;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

public class RDCStateRegistry
        extends BaseModelRegistry<RDCStateRegistry.RDCState> {

    private static final String FIELD_NAME_RDCID = "RDCID";

    @Override
    protected Class<RDCState> getModelClass() {
        return RDCState.class;
    }

    public RDCState getRDCStateForID(String RDCID) {
        return this.get(RDCID);
    }

    public static class RDCState extends DModel {

        @QuerySqlField(index = true)
        private String rdcID;

        @QuerySqlField
        private String rdcIP;

        @QuerySqlField
        private Integer rdcHealthStatus;

        @QuerySqlField
        private String rdcComponentHealth;

        @QuerySqlField
        private String lastUpdatedTime;

        public RDCState(RDCStateRegistryDto dto) {
            super(dto);
            this.rdcHealthStatus = dto.getRdcHealthStatus();
            this.lastUpdatedTime = dto.getLastUpdatedTime();
            this.rdcID = dto.getRdcID();
            this.rdcIP = dto.getRdcIP();
            this.rdcComponentHealth = dto.getRdcComponentHealth();
        }

        @Override
        public <K extends RegistryStateDTO> K toRegistryStateDTO() {
            return (K) new RDCStateRegistryDto(this);
        }

        public String getRdcID() {
            return rdcID;
        }


        public String getRdcIP() {
            return rdcIP;
        }

        public Integer getRdcHealthStatus() {
            return rdcHealthStatus;
        }


        public String getRdcComponentHealth() {
            return rdcComponentHealth;
        }


        public String getLastUpdatedTime() {
            return lastUpdatedTime;
        }


        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this,
                    ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }
}
