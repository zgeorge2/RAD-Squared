package com.rad2.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

public class TaskRunner {
    private int threadPoolSize;

    public TaskRunner(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    public void runTasks(List<Runnable> rList) {
        try {
            ExecutorService service = Executors.newFixedThreadPool(this.threadPoolSize);
            List<Future> futures = new ArrayList<>();
            // submit the tasks
            IntStream.range(0, rList.size()).forEach(i -> futures.add(service.submit(rList.get(i))));
            // wait for all tasks to complete before continuing
            futures.forEach(f -> {
                try {
                    f.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });
            //shut down the executor service so that this thread can exit
            service.shutdownNow();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
