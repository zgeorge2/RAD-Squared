package com.vmware.ignite.common.queries;

import org.apache.ignite.cache.query.SqlQuery;

import java.util.function.Supplier;

abstract public class IQuery<K, V> {
    private SqlQuery<K, V> query;

    public IQuery(Supplier<SqlQuery<K, V>> supplierFunc) {
        this.query = supplierFunc.get();
    }

    public final SqlQuery<K, V> getQuery() {
        return this.query;
    }
}
