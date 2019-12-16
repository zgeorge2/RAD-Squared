package com.vmware.vap.service.dto;

/**
 * Callback Response
 */
public class CallbackResponse {
    String message;

    public CallbackResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
