package com.rad2.akka.common;

import com.rad2.ctrl.deps.IJobRef;

/**
 * Represents a Deferred Request or Message. Every such deferred carries a JobRef
 * to handle timeouts and to help set results
 */
public interface IDeferred<T> {
    Object getArg(String key);

    IDeferred<T> putArg(String key, Object arg);

    IJobRef getJobRef();

    default String getJobRefRegId() {
        return getJobRef().regId();
    }

    void setResponse(T res);
}
