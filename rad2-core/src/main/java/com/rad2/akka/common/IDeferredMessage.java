package com.rad2.akka.common;

import com.rad2.ctrl.deps.IJobRef;

/**
 * Represents IDeferred instances on the Actor side. Every IDeferredMessage carries an
 * embedded iDeferredRequest, which ties it back to the original REST layer request.
 */
public interface IDeferredMessage<T> extends IDeferred<T> {
    IDeferredRequest<T> getEmbeddedRequest();

    default Object getArg(String key) {
        return getEmbeddedRequest().getArg(key);
    }

    default IDeferred<T> putArg(String key, Object arg) {
        return getEmbeddedRequest().putArg(key, arg);
    }

    default IJobRef getJobRef() {
        return getEmbeddedRequest().getJobRef();
    }

    default void setResponse(T res) {
        getEmbeddedRequest().setResponse(res);
    }
}
