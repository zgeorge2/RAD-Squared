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
 */
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
            PrintUtils.printToActor("FAILED ON ASK AND WAIT: [%s]", e.getMessage());
            e.printStackTrace();
        }
        return ret;
    }

    public O askAndWait(I input) {
        return this.askAndWait(input, DEFAULT_DURATION_IN_SECONDS);
    }
}
