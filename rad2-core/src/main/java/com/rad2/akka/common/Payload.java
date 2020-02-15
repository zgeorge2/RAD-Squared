package com.rad2.akka.common;

import java.io.Serializable;

enum PayloadType {
    Request,
    Response,
    Failure
}

public class Payload implements Serializable {
    private PayloadType payloadType;
    private String actorId;
    private Object data;

    public Payload(PayloadType payloadType) {
        this.payloadType = payloadType;
    }

    public Payload(PayloadType payloadType, Object data, String actorUUID) {
        this.payloadType = payloadType;
        this.data = data;
        this.actorId = actorUUID;
    }

    public PayloadType getPayloadType() {
        return payloadType;
    }

    public String getActorId() {
        return actorId;
    }

    public Object getData() {
        return data;
    }
}
