/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.akka.common;

import akka.actor.ActorSelection;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.rad2.common.utils.PrintUtils;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

/**
 * Instantiate an instance of this class to send async requests to Actors with "Input" messages and to receive
 * a response or error after a configurable duration, as "Output". Thus, this is a simple wrapper class to
 * perform ask and wait operations with Akka Actors. Please use SPARINGLY. Use when you need to get the result
 * of an operation synchronously and know for sure that the operation is short lived.
 * Where possible, always use IDeferredRequest for async handling.
 * Example:
 *     // implement this in the MyFooResource.java (extends BaseResource)
 *     // @GetMapping("/getSomeFoo")
 *     public Callable<ResponseEntity<PrintOut>> getSomeFoo() {
 *         return () -> ResponseEntity.ok(new PrintOut(getC().getSomeFoo()));
 *     }
 *
 *     // implement this in MyFooController.java (extends BaseController)
 *     public String getSomeFoo() {
 *         ActorSelection myFooActor = ...;
 *         AkkaAskAndWait<MyFooActor.GetSomeFoo, MyFooActor.GetSomeFooResult> ask = new AkkaAskAndWait<>(myFooActor);
 *         return ask.askAndWait(new MyFooActor.GetSomeFoo(), <args>).result();
 *     }
 */
@Deprecated
public class AkkaAskAndWait<I, O> {
    private static final long DEFAULT_DURATION_IN_SECONDS = 10;
    private ActorSelection actor;

    public AkkaAskAndWait(ActorSelection actor) {
        this.actor = actor;
    }

    public O askAndWait(I input, long durationInSeconds) {
        O ret = null;
        Timeout timeout = new Timeout(Duration.create(durationInSeconds, TimeUnit.SECONDS));
        try {
            ret = (O) Await.result(Patterns.ask(this.actor, input, timeout), timeout.duration());
        } catch (Exception e) {
            PrintUtils.print("FAILED ON ASK AND WAIT: [%s]", e.getMessage());
            e.printStackTrace();
        }
        return ret;
    }

    public O askAndWait(I input) {
        return this.askAndWait(input, DEFAULT_DURATION_IN_SECONDS);
    }
}
