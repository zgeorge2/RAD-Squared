package com.rad2.akka.common;

/**
 * Is a wrapper that transforms a IDeferredMessage<T> instance into a IDeferredMessage<T> instance.
 */
public class BasicDeferredMessage<T> implements IDeferredMessage<T> {
    private IDeferredRequest<T> req;

    public BasicDeferredMessage(IDeferredRequest<T> req) {
        this.req = req;
    }

    @Override
    public IDeferredRequest<T> getEmbeddedRequest() {
        return req;
    }
}
