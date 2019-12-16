package com.vmware.akka.sample;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import akka.actor.Props;
import com.vmware.common.serialization.IAkkaSerializable;
import com.vmware.common.utils.PrintUtils;

public class SampleActor extends AbstractActor {
    private String name;

    private SampleActor(String name) {
        this.name = name;
    }

    static public Props props(String name) {
        return Props.create(SampleActor.class, name);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(PrintAndForward.class, this::printAndForward)
            .match(Print.class, this::print)
            .build();
    }

    private void printAndForward(PrintAndForward p) {
        this.print(p);
        // send a message to the remote actor.
        p.forwardTo.tell(new SampleActor.Print("Hello Remote"), self());
    }

    private void print(Print p) {
        PrintUtils.printToActor("Received message: [%s] from [%s]", p.message, this.getSender().path());
    }

    /**
     * Classes used for received method above.
     */
    static public class PrintAndForward extends Print {
        public ActorSelection forwardTo;

        public PrintAndForward() {
            this(null, null);
        }

        public PrintAndForward(String message, ActorSelection forwardTo) {
            super(message);
            this.forwardTo = forwardTo;
        }
    }

    static public class Print implements IAkkaSerializable {
        public String message;

        public Print() {
            this(null);
        }

        public Print(String message) {
            this.message = message == null ? "<NO MESSAGE>" : message;
        }
    }
}

