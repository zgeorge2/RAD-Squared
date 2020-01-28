package com.rad2.ignite.common.queries;

import org.apache.ignite.cache.query.SqlQuery;

public class INamedQuery<K, V> extends IQuery<K, V> {
    public INamedQuery(Class modelClass, String selectQuery, Object... args) {
        super(() -> {
            SqlQuery<K, V> q = new SqlQuery<>(modelClass, selectQuery);
            return q.setArgs(args);
        });
    }
}
