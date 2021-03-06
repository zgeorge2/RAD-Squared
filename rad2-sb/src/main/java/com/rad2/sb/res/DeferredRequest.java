/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.sb.res;

import com.rad2.akka.common.IDeferredRequest;
import com.rad2.apps.adm.ignite.JobStatusEnum;
import com.rad2.ctrl.deps.IJobRef;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides a basic wrapper around a Spring request's args and the DeferredResult object. Result type is
 * String for this implementation. Only String results are supported at this time. TODO: extend for non-Strings
 */
public class DeferredRequest implements IDeferredRequest<String> {
    private final static long DEFAULT_TIMEOUT = 2000L;
    private final Map<String, Object> argsMap;
    private final DeferredResult<ResponseEntity<String>> result;
    private final IJobRef ijr; // set up a job ref in case of request timeout.

    public DeferredRequest(IJobRef ijr) {
        this(DEFAULT_TIMEOUT, ijr);
    }

    private DeferredRequest(long waitTimeForResult, IJobRef ijr) {
        argsMap = new HashMap<>();
        result = new DeferredResult<>(waitTimeForResult);
        this.ijr = ijr;
        // set default timeout handling
        String timeoutMessage = String.format(JobStatusEnum.JOB_TIMEOUT_FORMAT, this.ijr.regId());
        result.onTimeout(() -> result.setResult(ResponseEntity.ok(timeoutMessage)));
    }

    @Override
    public Map<String, Object> args() {
        return argsMap;
    }

    @Override
    public Object arg(String key) {
        return argsMap.get(key);
    }

    @Override
    public void putArg(String key, Object arg) {
        argsMap.put(key, arg);
    }

    @Override
    public IJobRef jobRef() {
        return ijr;
    }

    @Override
    public void setResponse(String res) {
        getResult().setResult(ResponseEntity.ok(res));
    }

    public DeferredResult<ResponseEntity<String>> getResult() {
        return result;
    }
}
