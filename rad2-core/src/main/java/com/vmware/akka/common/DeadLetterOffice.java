package com.vmware.akka.common;

import com.vmware.akka.aspects.ActorMessageHandler;
import com.vmware.ignite.common.RegistryManager;
import akka.actor.DeadLetter;
import akka.actor.Props;

public class DeadLetterOffice extends BaseActor{
	public DeadLetterOffice(RegistryManager rm) {
		super(rm);
	}
	
	static public Props props(RegistryManager rm) {
        return Props.create(DeadLetterOffice.class, rm);
    }
	
	@Override
	public Receive createReceive() {
		return super.createReceive()
        .orElse(receiveBuilder()
				.match(DeadLetter.class, this::deadLetterHandler)
				.build());
	}
	
	@ActorMessageHandler
	public void deadLetterHandler(DeadLetter dl) {
		
	}
}
