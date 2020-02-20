package com.rad2.akka.common;

import com.rad2.ctrl.deps.IJobRef;

import java.util.Map;

/**
 * Represents a Deferred Request or Message. Every such deferred carries a JobRef
 * to handle timeouts and to help set results
 */
public interface IDeferred<T> {
    Map<String, Object> args();

    default Object arg(String key) {
        return args().get(key);
    }

    default void putArg(String key, Object arg) {
        args().put(key, arg);
    }

    IJobRef jobRef();

    default String jobRegId() {
        return jobRef().regId();
    }
}
