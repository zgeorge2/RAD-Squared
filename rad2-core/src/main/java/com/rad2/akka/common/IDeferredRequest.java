/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.akka.common;

/**
 * Represents a IDeferred request on the REST side. Implemented using REST concepts.
 */
public interface IDeferredRequest<T> extends IDeferred<T> {
    void setResponse(T res);
}
