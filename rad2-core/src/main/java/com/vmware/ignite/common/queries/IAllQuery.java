package com.vmware.ignite.common.queries;

import org.apache.ignite.cache.query.SqlQuery;

public class IAllQuery<K, V> extends IQuery<K, V> {
    public IAllQuery(Class modelClass, String modelName) {
        super(() -> {
            String selectQuery = String.format("select * from %s", modelName);
            return new SqlQuery<>(modelClass, selectQuery);
        });
    }
}
