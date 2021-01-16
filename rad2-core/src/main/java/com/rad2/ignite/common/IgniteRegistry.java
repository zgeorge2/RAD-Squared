/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.ignite.common;

import com.rad2.common.utils.NTxHolder;
import com.rad2.common.utils.PrintUtils;
import com.rad2.ignite.common.queries.IQuery;
import org.apache.ignite.configuration.CacheConfiguration;

import java.util.List;
import java.util.function.Consumer;

public class IgniteRegistry<K, V> {
    private IgniteProvider.ICache<K, V> cache;
    private RegistryManager rm;

    IgniteRegistry(RegistryManager rm, String cacheConfigurationKey,
                   Consumer<CacheConfiguration> cacheConfigurer) {
        this.rm = rm;
        this.cache = this.rm.getIgniteProvider().createCache(cacheConfigurationKey, cacheConfigurer);
    }

    IgniteProvider.ILock createLock(K key) {
        return rm.getIgniteProvider().createLock(this.getCache(), key);
    }

    NTxHolder<IgniteProvider.ITx> createTx() {
        return this.rm.getIgniteProvider().createTx();
    }

    void broadcastMessage(String message) {
        this.rm.getIgniteProvider().broadcast(() -> PrintUtils.print(message));
    }

    V getValue(K key) {
        return this.getCache().getValue(key);
    }

    void putValue(K key, V value) {
        this.getCache().putValue(key, value);
    }

    boolean remove(K key) {
        return this.getCache().remove(key);
    }

    V getOne(IQuery<K, V> query) {
        return this.getCache().getOne(query);
    }

    public List<V> getAll(IQuery<K, V> query) {
        return this.getCache().getAll(query);
    }

    private IgniteProvider.ICache<K, V> getCache() {
        return cache;
    }
}
