package com.vmware.common.utils;

import junit.framework.TestCase;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NTxHolderTest extends TestCase {
    public void testMultiLevelNestedTransactions() {
        Supplier<Integer> supp = () -> (int) (Math.random() * 100D);
        Consumer<Integer> cons = i -> System.out.println("Destroyed:" + i);
        NTxHolder<Integer> holder = new NTxHolder<>(supp, cons);
        List<Thread> threads = IntStream.range(0, 100).boxed()
            .map(i -> new Thread(new MyRunnable("Runner" + i, holder)))
            .collect(Collectors.toList());

        threads.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
            }
        });

        assertTrue(holder.isEmpty());
    }

    public static class MyRunnable implements Runnable {
        private NTxHolder<Integer> holder;
        private String name;

        MyRunnable(String name, NTxHolder<Integer> holder) {
            this.name = name;
            this.holder = holder;
        }

        private String print(NTxHolder<Integer> ntx) {
            return String.format("Name:%s [NTx:%s]", this.name, ntx);
        }

        @Override
        public void run() {
            try (NTxHolder<Integer> t1 = holder.open()) {
                try (NTxHolder<Integer> t2 = holder.open()) {
                    try (NTxHolder<Integer> t3 = holder.open()) {
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}