package com.rad2.akka.router;

import com.rad2.ignite.common.RegistryManager;

import java.util.HashMap;
import java.util.Map;

/**
 * WorkerClassArgs carries arguments to the WorkerActor (actually its child class) (including the name of that
 * child class). As with all other Actors, the Props object reaching the WorkerActor MUST Be immutable . So
 * while the WorkerClassArgs allows for arbitray key-value pairs to be sent to the child WorkerActor class,
 * those key-value pairs need to be immutable, just as the WorkerClassArgs is.
 */
public class WorkerClassArgs {
    private static final String ID_KEY = "ID_KEY";
    private static final String RM_KEY = "REGISTRY_MANAGER";
    private Map<String, Object> argsMap; // this map and its contents MUST be immutable

    public WorkerClassArgs(String id, RegistryManager rm) {
        this.argsMap = new HashMap<>();
        this.argsMap.put(ID_KEY, id);
        this.argsMap.put(RM_KEY, rm);
    }

    /**
     * entries added through this method can override the value previously set by the constructor
     */
    public void put(String key, Object val) {
        this.argsMap.put(key, val);
    }

    public String getId() {
        return (String) this.argsMap.get(ID_KEY);
    }

    public RegistryManager getRM() {
        return (RegistryManager) this.argsMap.get(RM_KEY);
    }

    /**
     * @return Object corresponding to key, null if none are found
     */
    public Object getArg(String key) {
        return argsMap.get(key);
    }
}
