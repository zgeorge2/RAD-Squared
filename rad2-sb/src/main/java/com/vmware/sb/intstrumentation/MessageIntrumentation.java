package com.vmware.sb.intstrumentation;

import java.util.HashMap;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

import akka.actor.AbstractActor;
import akka.actor.DeadLetter;
import kamon.Kamon;

@Aspect
public class MessageIntrumentation {
	@Around("execution(* *(..)) && @annotation(com.vmware.akka.aspects.ActorMessageHandler)")
	public Object observeMessage(ProceedingJoinPoint jp) throws Throwable{
		// Time the taken by the receiver method in the actor
		long start = System.currentTimeMillis()*1000;
		Object output = jp.proceed();
		long elapsedTime = System.currentTimeMillis()*1000 - start;

		// Get the base class for 
		AbstractActor abstractActor = (AbstractActor) jp.getThis();
		
		String messageClass;
		String message;
		String senderPath;
		String recieverPath;
		
		boolean isRemoteMessage = false;
		boolean isDeadLetter = false;
		
		HashMap<String, String> tags = new HashMap<>();
		
		// Prepare the Tag Values for the metrics
		if(jp.getArgs()[0] instanceof DeadLetter) {
			isDeadLetter = true;
			DeadLetter deadLetter = (DeadLetter) jp.getArgs()[0];
			messageClass = deadLetter.getClass().toString().split(" ")[1];
			message = deadLetter.message().getClass().getSimpleName();
			senderPath = deadLetter.sender().path().toString();
			recieverPath = deadLetter.recipient().path().toString().split("://")[1];
			
		} else {
			messageClass = jp.getArgs()[0].getClass().getSimpleName();
			message = jp.getArgs()[0].getClass().getSimpleName();
			senderPath = abstractActor.getSender().path().toString();
			recieverPath = abstractActor.getSelf().path().toString().split("://")[1];
		}
		
		// Check if the communication was Remote
		if(senderPath.split("://")[0].equals("akka.tcp")) {
			isRemoteMessage = true;
			String tempSenderPath = senderPath.split("://")[1];
			String[] splitSenderPath = tempSenderPath.split("/");
	        splitSenderPath[0] = splitSenderPath[0].split("@")[0];
	        senderPath = String.join("/",splitSenderPath);
		} else {
			senderPath = senderPath.split("://")[1];
		}
		
		
		// Insert tag values into HashMap
		tags.put("message", message);
		tags.put("senderPath", senderPath);
		tags.put("senderSystem", senderPath.split("/")[0]);
		tags.put("recieverPath", recieverPath);
		tags.put("recieverSystem", recieverPath.split("/")[0]);
		tags.put("class", messageClass);
		
		if(isDeadLetter) {
			// Counter for Dead Letter
			Kamon.counter("akka.message.dead-letters")
				.refine(tags)
				.increment();
		} else {
			if(isRemoteMessage) {
				// Histogram for Remote Messages
				Kamon.histogram("akka.remote.message.processing-time")
					.refine(tags)
					.record(elapsedTime);
			} else {
				// Histogram for Local Messages
				Kamon.histogram("akka.message.processing-time")
				.refine(tags)
				.record(elapsedTime);
			}
		}
		
		return output;
	}	
}
