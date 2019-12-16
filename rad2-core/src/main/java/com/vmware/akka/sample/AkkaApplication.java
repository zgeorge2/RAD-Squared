package com.vmware.akka.sample;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.IOException;

public class AkkaApplication {
    private ActorSystem system;

    public AkkaApplication(String appConfigName) {
        // load the appconfig
        Config config = ConfigFactory.load(appConfigName);
        String locSysName = config.getString("sample.locSysName");
        String remSysName = config.getString("sample.remSysName");
        String locPort = config.getString("sample.locPort");
        String remPort = config.getString("sample.remPort");
        String locActorName = config.getString("sample.locActorName");
        String remActorName = config.getString("sample.remActorName");

        // create the local actor system
        this.system = ActorSystem.create(locSysName, config);

        // setup paths to local and remote actors
        String locPath = "akka://" + locSysName + "/user/" + locActorName;
        String remPath = "akka.tcp://" + remSysName + "@127.0.0.1:" + remPort + "/user/" + remActorName;

        // create the local actor
        ActorRef locActor = this.system.actorOf(SampleActor.props(locActorName), locActorName);
        // create the actorSelection to the remote actor
        ActorSelection remActor = this.system.actorSelection(remPath);

        // sleep for a while - this is just to give time for the other App to come up
        try {
            System.out.println("Start the other process ... <press enter when ready>");
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        locActor.tell(new SampleActor.PrintAndForward("Hello Local", remActor), ActorRef.noSender());
    }

    public static void main(String[] args) {
        String appConfigName = args[0];
        AkkaApplication appA = new AkkaApplication(appConfigName);

        // sleep for a while - this is just to give time for the other App to come up
        try {
            System.out.println("Wait for the remote call to complete ... <press enter when ready>");
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
