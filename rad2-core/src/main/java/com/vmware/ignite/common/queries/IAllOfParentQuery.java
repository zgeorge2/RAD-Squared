package com.vmware.ignite.common.queries;

import org.apache.ignite.cache.query.SqlQuery;

public class IAllOfParentQuery<K, V> extends IQuery<K, V> {
    public IAllOfParentQuery(Class modelClass, String modelName, String orderBy, Object... args) {
        super(() -> {
            String selectQuery = String.format("select * from %s where parentKey=? ORDER BY %s",
                modelName, orderBy);
            SqlQuery<K, V> q = new SqlQuery<>(modelClass, selectQuery);
            return q.setArgs(args);
        });
    }
}
