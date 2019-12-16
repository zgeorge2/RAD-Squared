package com.vmware.ignite.common.queries;

import org.apache.ignite.cache.query.SqlQuery;

public class ISingleQuery<K, V> extends IQuery<K, V> {
    public ISingleQuery(Class modelClass, String arg) {
        super(() -> {
            String qVal = "key = ?";
            SqlQuery<K, V> q = new SqlQuery<>(modelClass, qVal);
            return q.setArgs(arg);
        });
    }
}
