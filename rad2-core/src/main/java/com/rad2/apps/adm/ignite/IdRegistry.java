package com.rad2.apps.adm.ignite;

import com.rad2.akka.common.RegistryStateDTO;
import com.rad2.ignite.common.BaseModelRegistry;
import com.rad2.ignite.common.DModel;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

public class IdRegistry extends BaseModelRegistry<IdRegistry.DIdRegModel> {
    @Override
    protected Class getModelClass() {
        return DIdRegModel.class;
    }

    /**
     * increment and get the id for the given keys
     *
     * @return
     */
    public long incrementAndGet(String key) {
        return this.apply(key, cc -> cc.incrementAndGet());
    }

    /**
     * The Model class and the registry state DTO
     */
    public static class DIdRegModel extends DModel {
        @QuerySqlField
        private long idRegLastValue;

        public DIdRegModel(IdRegistryStateDTO dto) {
            super(dto);
            this.idRegLastValue = 0l;
        }

        @Override
        public RegistryStateDTO toRegistryStateDTO() {
            return new IdRegistryStateDTO(this);
        }

        public long incrementAndGet() {
            this.idRegLastValue++;
            return this.idRegLastValue;
        }

        public long getLastId() {
            return this.idRegLastValue;
        }
    }

    public static class IdRegistryStateDTO extends RegistryStateDTO {
        public static final String ATTR_ID_REG_LAST_VALUE_KEY = "ID_REG_LAST_VALUE_KEY";

        public IdRegistryStateDTO(String parentKey, String name) {
            super(IdRegistry.class, parentKey, name);
            this.putAttr(ATTR_ID_REG_LAST_VALUE_KEY, 0l);
        }

        public IdRegistryStateDTO(DIdRegModel model) {
            super(IdRegistry.class, model);
            this.putAttr(ATTR_ID_REG_LAST_VALUE_KEY, model.getLastId());
        }

        @Override
        public DModel toModel() {
            return new DIdRegModel(this);
        }

        public long getLastId() {
            return (long) this.getAttr(ATTR_ID_REG_LAST_VALUE_KEY);
        }
    }
}
