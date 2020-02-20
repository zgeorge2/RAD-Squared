package com.rad2.akka.common;

import com.rad2.common.serialization.IAkkaSerializable;
import com.rad2.ctrl.deps.IJobRef;
import com.rad2.ctrl.deps.JobRef;

import java.util.HashMap;
import java.util.Map;

/**
 * Implements the Composite part of the IDeferred<T> Component
 */
public class BasicDeferredMessage<T> implements IDeferred<T>, IAkkaSerializable {
    private Map<String, Object> argsMap;
    private JobRef ijr;

    public BasicDeferredMessage(IDeferred<T> def) {
        this();
        if (def != null) {
            this.argsMap.putAll(def.args()); // always transfer from def to local map
            this.ijr = new JobRef(def.jobRef()); // always transfer from def to local ijr
        }
    }

    public BasicDeferredMessage() {
        this.argsMap = new HashMap<>();
        this.ijr = new JobRef();
    }

    @Override
    public Map<String, Object> args() {
        return this.argsMap;
    }

    @Override
    public IJobRef jobRef() {
        return ijr;
    }
}
