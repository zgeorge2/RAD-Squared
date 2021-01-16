/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.sb.res;

import com.rad2.akka.common.IDeferred;
import com.rad2.common.utils.PrintUtils;
import com.rad2.ctrl.BaseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseResource<K extends BaseController> {
    protected static final Logger logger = LoggerFactory.getLogger(BaseResource.class);
    private static final Pattern classNamePattern = Pattern.compile("^([a-zA-Z0-9]+)(Resource)");
    private K controller;

    public BaseResource() {
        PrintUtils.print("*** Creating  instance of %s ***", this.getClass());
    }

    public void initialize(K controller) {
        PrintUtils.print("*** Initializing [%s] with controller [%s]  ***", this.getClass(),
                controller);
        this.controller = controller;
    }

    protected K getC() {
        return controller;
    }

    public final String getTypePrefix() {
        Matcher m = classNamePattern.matcher(this.getClass().getSimpleName());
        return m.find() ? m.group(1) : null;
    }

    /**
     * Create a new DeferredRequest instance for use with the Controller.
     * Every REST request is encapsulated into an IDeferredRequest<String>
     * implementation before it is sent to the Controller
     */
    public DeferredResult<ResponseEntity<String>> createRequest(Consumer<IDeferred<String>> nextStep) {
        return createRequest(null, nextStep);
    }

    /**
     * Create a new DeferredRequest instance for use with the Controller.
     * Every REST request is encapsulated into an IDeferredRequest<String>
     * implementation before it is sent to the Controller
     */
    public DeferredResult<ResponseEntity<String>> createRequest(Consumer<IDeferred<String>> populateArgs,
                                                                Consumer<IDeferred<String>> nextStep) {
        DeferredRequest req = new DeferredRequest(getC().createJobRef());
        if (populateArgs != null) populateArgs.accept(req); // first populate the args
        getC().initJob(req, req::setResponse, nextStep); // next init the job. the job init will trigger nextStep
        return req.getResult();
    }

    /**
     * Create a DeferredRequest instance to retrieve a Job Result. This overloaded version
     * only sets up a consumer for the result of the request.
     */
    protected DeferredResult<ResponseEntity<String>> retrieveJobResult(String pKey, String name) {
        DeferredRequest req = new DeferredRequest(getC().createJobRef(pKey, name));
        getC().initJobRetrieval(req, req::setResponse, r -> getC().getJobResult(r));
        return req.getResult();
    }
}
