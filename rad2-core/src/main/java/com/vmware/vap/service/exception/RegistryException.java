package com.vmware.vap.service.exception;

public class RegistryException extends Exception {

    public RegistryException(String message) {
        super(message);
    }

    public RegistryException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
