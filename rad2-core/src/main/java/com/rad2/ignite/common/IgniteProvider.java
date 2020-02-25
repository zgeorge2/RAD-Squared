/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.ignite.common;

import com.rad2.akka.common.SystemProperties;
import com.rad2.common.utils.NTxHolder;
import com.rad2.common.utils.PrintUtils;
import com.rad2.ignite.common.queries.IQuery;
import org.apache.ignite.*;
import org.apache.ignite.cache.query.SqlQuery;
import org.apache.ignite.configuration.AtomicConfiguration;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.lang.IgniteRunnable;
import org.apache.ignite.lifecycle.LifecycleBean;
import org.apache.ignite.lifecycle.LifecycleEventType;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.apache.ignite.spi.eventstorage.EventStorageSpi;
import org.apache.ignite.spi.eventstorage.memory.MemoryEventStorageSpi;
import org.apache.ignite.transactions.Transaction;
import org.apache.ignite.transactions.TransactionConcurrency;
import org.apache.ignite.transactions.TransactionIsolation;

import javax.cache.Cache;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class IgniteProvider {
    private static String DEFAULT_ID_GEN_SEQUENCE_NAME = "DEFAULT_ID_GENERATOR___";
    private Ignite ignite;
    private SystemProperties sysProps;
    private NTxHolder<ITx> txHolder;
    private Map<String, IAtomicLong> idGenMap;

    public IgniteProvider(SystemProperties sysProps) {
        PrintUtils.printToActor("*** Creating the IgniteProvider!!! ***");
        this.sysProps = sysProps;
        this.ignite = start(getIgniteConfiguration());
        // create the transaction holder
        this.txHolder = new NTxHolder<>(() -> new ITx(this.ignite), IgniteProvider.ITx::close);
        // create the sequence number generator map (distributed)
        this.idGenMap = new ConcurrentHashMap<>();
    }

    public boolean shutdown() {
        Ignition.stop(true);
        PrintUtils.printToActor("Shutting down Ignite nodes");
        return true;
    }

    NTxHolder<ITx> createTx() {
        return this.txHolder.open();
    }

    <K> ILock createLock(ICache cache, K key) {
        return new ILock(cache, key);
    }

    <K, V> ICache<K, V> createCache(String cacheConfigurationKey,
                                    Consumer<CacheConfiguration> cacheConfigurer) {
        return new ICache<>(this.get(), cacheConfigurationKey, cacheConfigurer);
    }

    IAtomicLong getIdGen() {
        return this.getIdGen(DEFAULT_ID_GEN_SEQUENCE_NAME);
    }

    synchronized IAtomicLong getIdGen(String sequenceName) {
        IAtomicLong ret = this.idGenMap.get(sequenceName);
        if (ret == null) {
            ret = new IAtomicLong(this.get(), sequenceName);
            this.idGenMap.put(sequenceName, ret);
        }
        return ret;
    }

    void broadcast(IgniteRunnable arg) {
        IgniteCompute compute = this.get().compute();
        compute.broadcast(arg);
    }

    @NotNull
    private IgniteConfiguration getIgniteConfiguration() {
        // set Ignite system properties (changes from default)
        this.setIgniteSystemProperties();
        IgniteConfiguration cfg = new IgniteConfiguration();
        // set the configuration of this ignite node to use the data storage configuration
        cfg.setDataStorageConfiguration(this.getDataStorageConfiguration());
        // set the lifecycle bean
        cfg.setLifecycleBeans(getLifecycleBean());
        // set the EventStorageSpi
        cfg.setEventStorageSpi(getEventStorageSpi());
        // set the atomic long configuration
        cfg.setAtomicConfiguration(this.getAtomicConfiguration());
        // setup no logging
        cfg.setMetricsLogFrequency(0);
        // Override default discovery SPI.
        cfg.setDiscoverySpi(getDiscoverySpi());
        // set consistent id in configuration
        cfg.setConsistentId(this.sysProps.getSystemId());
        return cfg;
    }

    private Ignite start(IgniteConfiguration cfg) {
        // start ignite
        Ignite ig = Ignition.start(cfg);
        // activate the cluster. Automatic topology initialization occurs only if you manually
        // activate the cluster for the very first time
        ig.cluster().active(true);
        return ig;
    }

    private Ignite get() {
        return this.ignite;
    }

    private void setIgniteSystemProperties() {
        if (IgniteSystemProperties.getInteger(IgniteSystemProperties.IGNITE_MAX_INDEX_PAYLOAD_SIZE, -1) == -1) {
            System.setProperty(IgniteSystemProperties.IGNITE_MAX_INDEX_PAYLOAD_SIZE, "100");
        }
    }

    @NotNull
    private TcpDiscoverySpi getDiscoverySpi() {
        // use the multicast ipfinder
        TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();
        // Set initial IP multicast addresses.

        if(this.sysProps.getDiscoveryMode().equals("local")) {
            PrintUtils.printToActor("*************** Ignite Cluster running on localhost");
            ipFinder.setAddresses(Collections.singletonList("localhost"));
        } else if(this.sysProps.getDiscoveryMode().equals("remote")){
            PrintUtils.printToActor("*************** Ignite Cluster running on different machines");
            ipFinder.setAddresses(this.sysProps.getIgniteMachines());
        }
        //ipFinder.setMulticastGroup("224.0.0.1");

        return new TcpDiscoverySpi().setIpFinder(ipFinder);
    }

    @NotNull
    private DataStorageConfiguration getDataStorageConfiguration() {
        // Configure this to be BOTH an in-memory DB and to use the disk
        DataStorageConfiguration dsc = new DataStorageConfiguration();
        dsc.getDefaultDataRegionConfiguration().setPersistenceEnabled(true);
        return dsc;
    }

    @NotNull
    private LifecycleBean getLifecycleBean() {
        return new ILifecycleBean();
    }

    @NotNull
    private EventStorageSpi getEventStorageSpi() {
        MemoryEventStorageSpi spi = new MemoryEventStorageSpi();
        spi.setExpireCount(4000); // TODO remove hardcoding. get from res file
        return spi;
    }

    @NotNull
    private AtomicConfiguration getAtomicConfiguration() {
        AtomicConfiguration atomicCfg = new AtomicConfiguration();
        // Set number of backups.
        atomicCfg.setBackups(1);
        // Set number of sequence values to be reserved locally at a time (avoids this many sync calls
        // with
        // other nodes)
        atomicCfg.setAtomicSequenceReserveSize(5000);
        return atomicCfg;
    }

    /**
     * Wrapper classes around Ignite constructs
     */
    public static class ILifecycleBean implements LifecycleBean {
        @Override
        public void onLifecycleEvent(LifecycleEventType evt) {
            if (evt == LifecycleEventType.BEFORE_NODE_START) {
                PrintUtils.printToActor("Received BEFORE_NODE_START");
            } else if (evt == LifecycleEventType.AFTER_NODE_START) {
                PrintUtils.printToActor("Received AFTER_NODE_START");
            } else if (evt == LifecycleEventType.BEFORE_NODE_STOP) {
                PrintUtils.printToActor("Received BEFORE_NODE_STOP");
            } else if (evt == LifecycleEventType.AFTER_NODE_STOP) {
                PrintUtils.printToActor("Received AFTER_NODE_STOP");
            }
        }
    }

    public static class IAtomicLong {
        private Ignite ignite;
        private String sequenceName;
        private IgniteAtomicSequence seq;

        IAtomicLong(Ignite ignite, String sequenceName) {
            this.ignite = ignite;
            this.sequenceName = sequenceName;
        }

        public long generateNewId() {
            this.initialize();
            return this.seq.incrementAndGet();
        }

        /**
         * Lazy initialize the ignite atomic sequence
         */
        private final void initialize() {
            if (this.seq == null || this.seq.removed()) {
                this.seq = ignite.atomicSequence(sequenceName, 0, true);
            }
        }
    }

    static class ITx {
        private Transaction tx;

        ITx(Ignite ignite) {
            this.tx = ignite.transactions()
                .txStart(TransactionConcurrency.PESSIMISTIC, TransactionIsolation.REPEATABLE_READ);
        }

        public void close() {
            this.tx.commit();
        }
    }

    static class ILock<K, V> implements AutoCloseable {
        private Lock lock;

        ILock(ICache<K, V> cache, K key) {
            this.lock = cache.lock(key); // get the lock
            this.lock.lock(); // acquire the lock
        }

        @Override
        public void close() throws Exception {
            this.lock.unlock();
        }
    }

    static class ICache<K, V> {
        private IgniteCache<K, V> cache;

        ICache(Ignite ignite, String cacheConfigurationKey,
               Consumer<CacheConfiguration> cacheConfigurer) {
            CacheConfiguration<K, V> cacheConfig =
                new CacheConfiguration<>(cacheConfigurationKey);
            cacheConfigurer.accept(cacheConfig); // do any additional configuration in the client
            this.cache = this.getOrCreateCache(ignite, cacheConfig);
        }

        V getValue(K key) {
            return this.getCache().get(key);
        }

        void putValue(K key, V value) {
            this.getCache().put(key, value);
        }

        boolean remove(K key) {
            return this.getCache().remove(key);
        }

        Lock lock(K key) {
            return this.getCache().lock(key);
        }

        V getOne(IQuery<K, V> query) {
            return this.query(query.getQuery()).stream().map(Cache.Entry::getValue).findAny().orElse(null);
        }

        List<V> getAll(IQuery<K, V> query) {
            return this.query(query.getQuery()).stream().map(Cache.Entry::getValue).collect(Collectors.toList());
        }

        private IgniteCache<K, V> getCache() {
            return cache;
        }

        private List<Cache.Entry<K, V>> query(SqlQuery<K, V> q) {
            return this.getCache().query(q).getAll();
        }

        private IgniteCache<K, V> getOrCreateCache(Ignite ignite, CacheConfiguration<K, V> configuration) {
            return ignite.getOrCreateCache(configuration);
        }
    }
}
