package com.rad2.sb.res;

import com.rad2.akka.common.IDeferredRequest;
import com.rad2.ctrl.deps.IJobRef;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides a basic wrapper around a Spring request's args and the DeferredResult object
 */
public class DeferredRequest implements IDeferredRequest<String> {
    private static long DEFAULT_TIMEOUT = 2000L;
    private Map<String, Object> argsMap;
    private DeferredResult<ResponseEntity<String>> result;
    private IJobRef ijr; // set up a job ref in case the request times out.

    public DeferredRequest(IJobRef ijr) {
        this(DEFAULT_TIMEOUT, ijr);
    }

    public DeferredRequest(long waitTimeForResult, IJobRef ijr) {
        argsMap = new HashMap<>();
        result = new DeferredResult<>(waitTimeForResult);
        this.ijr = ijr;
        // set default timeout handling
        String timeoutMessage = String.format("Request has timed out.\nUse:[/adm/getJobResult/%s]", this.ijr.regId());
        result.onTimeout(() -> result.setResult(ResponseEntity.ok(timeoutMessage)));
    }

    @Override
    public Object getArg(String key) {
        return argsMap.get(key);
    }

    @Override
    public IDeferredRequest<String> putArg(String key, Object arg) {
        argsMap.put(key, arg);
        return this;
    }

    @Override
    public IJobRef getJobRef() {
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
