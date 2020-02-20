package com.rad2.apps.adm.akka;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * This singleton transiently stores IJobRef's to Consumers of JobResults
 */
public class JobConsumers {
    private static JobConsumers inst;
    // A in-memory map to store jobrefs to a result consumers
    private Map<String, List<Consumer<String>>> jobsToCons;

    public static synchronized JobConsumers getInstance() {
        if (inst == null) {
            inst = new JobConsumers();
        }
        return inst;
    }

    private JobConsumers() {
        this.jobsToCons = new HashMap<>();
    }

    synchronized void addConsumer(JobTrackerWorker.AddConsumer arg) {
        List<Consumer<String>> l = getConsFor(arg.regId());
        l.add(arg.cons);
    }

    synchronized void removeConsumers(JobTrackerWorker.RemoveConsumers arg) {
        List<Consumer<String>> l = getConsFor(arg.regId());
        l.clear(); // clear the list
        jobConsMap().remove(arg.regId()); // clear the map of this regId
    }

    synchronized void notifyConsumers(JobTrackerWorker.NotifyConsumers arg) {
        getConsFor(arg.regId()).forEach(cons -> cons.accept(arg.result));
    }

    private Map<String, List<Consumer<String>>> jobConsMap() {
        return jobsToCons;
    }

    private List<Consumer<String>> getConsFor(String regId) {
        return jobConsMap().computeIfAbsent(regId, k -> new ArrayList<>());
    }
}
