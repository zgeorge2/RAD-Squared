package com.vmware.common.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * NTxHolder is a generic class for handling Nested Transactions (of any kind, not necessarily for a database.
 * The idea of this holder is to provide the same instance of a given type (V) in a particular calling thread.
 * Since the holder is AutoCloseable, it can be used within a try block and nested several levels deep. The
 * supplier and destroyer functions to create instances of the value for each thread (once) and to destroy the
 * instance created when the transactions closes (all nested levels close).
 */
public class NTxHolder<V> implements AutoCloseable {
    private Supplier<V> vSupplier;
    private Consumer<V> vDestroyer;
    private ConcurrentHashMap<Thread, NTx<V>> tl;

    public NTxHolder(Supplier<V> vSupplier, Consumer<V> vDestroyer) {
        this.vSupplier = vSupplier;
        this.vDestroyer = vDestroyer;
        this.tl = new ConcurrentHashMap<>();
    }

    public NTxHolder<V> open() {
        if (get() == null) {
            this.put(new NTx<>(this.vSupplier.get(), this.vDestroyer));
        }
        get().open();
        return this;
    }

    public boolean isEmpty() {
        return this.tl.isEmpty();
    }

    @Override
    public void close() {
        NTx<V> ntx = get();
        if (ntx != null) {
            if (ntx.close()) {
                remove();
            }
        }
    }

    private NTx<V> get() {
        return this.tl.get(Thread.currentThread());
    }

    private void put(NTx<V> val) {
        this.tl.put(Thread.currentThread(), val);
    }

    private void remove() {
        this.tl.remove(Thread.currentThread());
    }

    /**
     * The NTx class holds the actual instance of generic param V and maintains reference counting to it. Thus
     * an instance can be used in a Nested Transaction (several levels deep).
     */
    public static class NTx<V> {
        private V value;
        private int references;
        private Consumer<V> destroyer;

        public NTx(V value, Consumer<V> destroyer) {
            this.value = value;
            this.destroyer = destroyer;
        }

        public NTx<V> open() {
            //PrintUtils.printToActor("OPENING:%s", this);
            this.references++;
            return this;
        }

        public V getValue() {
            return this.value;
        }

        public boolean close() {
            boolean actuallyClosed = false;
            this.references--;
            //PrintUtils.printToActor("CLOSING:%s", this);
            if (this.references == 0) {
                this.destroyer.accept(this.value);
                actuallyClosed = true;
            }
            return actuallyClosed;
        }

        @Override
        public String toString() {
            return String.format("value=[%s][ref=%s]", this.value, this.references);
        }
    }
}
