/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.akka.common;

import akka.actor.DeadLetter;
import akka.actor.Props;
import com.rad2.akka.aspects.ActorMessageHandler;
import com.rad2.ignite.common.RegistryManager;

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
