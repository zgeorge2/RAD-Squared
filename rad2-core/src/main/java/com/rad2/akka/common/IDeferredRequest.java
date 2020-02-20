package com.rad2.akka.common;

/**
 * Represents a IDeferred request on the REST side. Implemented using REST concepts.
 */
public interface IDeferredRequest<T> extends IDeferred<T> {
    void setResponse(T res);
}
