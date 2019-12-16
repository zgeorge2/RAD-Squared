package com.vmware.vap.service.message;

/**
 * Type of messages streams created on Lemans to receive messages from on-prem and are processed in SaaS and undergo
 * to the persistence.
 */
public enum MessagesStreamType {
    CONTROL_PLANE_ACTION,
    ENDPOINT_STATE,
    DATA_PLANE
}
