package com.rad2.akka.common;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import com.rad2.ignite.common.RegistryManager;

import java.time.Duration;


/**
 * For each request that comes to REST layer, REST layer spawns a AsyncAskActor to cater to that request.
 * REST layer makes ASK call to AsyncAskActor which will forward the message to desired `toRef` Actor in
 * the place of REST layer itself and AsyncAskActor will wait for a response to be returned asynchronously
 * or it will terminate itself and returning timeout failure back to REST layer. AsyncAskActor always
 * returns Either a valid response or failure back to the REST layer.
 */
public class AsyncAskActor extends BaseActor {

    private ActorRef toRef;
    private ActorRef fromRef;

    protected AsyncAskActor(RegistryManager rm, Duration duration, ActorRef toRef) {
        super(rm);
        SuicideRunnable runnable = new SuicideRunnable(getSelf());
        this.getContext().getSystem().scheduler().scheduleOnce(duration, runnable, this.getContext().getSystem().dispatcher());
        this.toRef = toRef;
    }

    /**
     *  Create runnable to Kill self in some duration if no data is returned back to AsyncAskActor
     */
    private class SuicideRunnable implements Runnable {
        private ActorRef ref;
        SuicideRunnable(ActorRef ref) {
            this.ref = ref;
        }
        public void run() {
            ref.tell(new Payload(PayloadType.Failure), ref);
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Payload.class, payload -> this.processPayload(payload))
                .build();
    }

    private void processPayload(Payload payload) {
        if ( payload.getPayloadType() == PayloadType.Request ) {
            this.fromRef = getSender();
            forwardReq(payload);
        } else {
            returnReq(payload);
            getSelf().tell(PoisonPill.getInstance(), ActorRef.noSender());
        }
    }

    private void forwardReq(Payload payload) {
        toRef.tell(payload, getSelf());
    }

    private void returnReq(Payload payload) {
        fromRef.tell(payload, getSelf());
    }
}
