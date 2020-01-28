package com.rad2.ignite.common.queries;

import org.apache.ignite.cache.query.SqlQuery;

public class IAllWithNameQuery<K, V> extends IQuery<K, V> {
    public IAllWithNameQuery(Class modelClass, Object... args) {
        super(() -> {
            String selectQuery = "name=?";
            SqlQuery<K, V> q = new SqlQuery<>(modelClass, selectQuery);
            return q.setArgs(args);
        });
    }
}
