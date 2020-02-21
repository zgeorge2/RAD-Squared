# Observability Platform for the RAD-Squared
Before This, check out this link to setup the RAD-Squared cluster

This repository holds the code for the Observability Platform for the RAD-Squared Framework.

The observability platform is developed on node.js on the server side and mongodb as the database.

Before actually running the Application, there are certain pre-requisites that need to be done.

1. You have to setup a mongo instance where the data can be persisted locally.
1. And lastly have the latest version of node and npm installed on your system.


## Setting Up MongoDb instance
You can install mongodb by running
```
brew tap mongodb/brew
brew install mongodb-community@4.0
```

You may run the following command on command prompt to check if the mongo service is running
```
mongo
```
If this fails to run, then check if the mongo server is up and running. You may run it by the command below.
```
mongod
```

You may also want to run mongod as a service on your machine for ease.

## Installing Node and Npm
Install nodejs by running this commmand
```
brew install node
```

The node installation can be checked by running
```
node -v
npm -v
```

## Running the Observability Platform
Run the Following command to install all the dependencies of the node application using npm.
```
npm install
```

and run the following command to start the app
```
npm start
```
If everything was set up correctly, then the platform should be running correctly. Visit (https://localhost:9425/) to view the run the application and see if it is working. 

### Setting up wavefront proxy(Optional)
You can install the proxy by running
```
brew install proxy
```

You will need to configure the proxy to send metrics to you wavefront cluster, you can change the parameters in this file
```
/usr/local/etc/wavefront/wavefront-proxy/wavefront.conf
```

The parameters to change are
```
server=<Wavefront_URL>
token=<API_KEY>
```

You may check if the proxy is running by running the following command.(netcat must be installed and added in the path variables to run the below command)
```
echo -e "test.metric 1 source=test_host\n" | nc <wavefront_proxy_address> 2878
```
Follow [this](https://docs.wavefront.com/proxies_manual_install.html) for more help.


## Configurations
The config file is located at /config/masterconfig.js.

#### Wavefront
Setting *wavefront.sendMetrics* to true enables the platform to also send metrics to wavefront.

You can configure the proxy by setting *wavefront.proxy* paramter
```
proxy : {
	host : '10.196.77.208',
	port : 2878
}

```

For using the wavefront alert manager, you can set the option *wavefront.alerting.enable* to use that. This poll the alerts on wavefront to determine the performance of actors and indicate violations by changing the color of the graph node representing that actor.

You also have to specify the api key and cluster url to make this feature work.
```
wavefront.clusterName = "<WAVEFRONT_URL>"
wavefront.apiKey = "<API_KEY>"
```

You can also configure the alert structure's depending upon what type of alert you want to create.
```
alerting : {
	alerts : [{
		name : 'Processing-Time-Threshold-Warn',
		body : '{ "name": "Processing-Time-Threshold-Warn", "condition": "ts(RAD.metricName.akka.actor.processing-time.*.max) > 100000", "minutes": 1, "resolveAfterMinutes": 2, "severity": "WARN" }'
	},
	{
		name : 'Processing-Time-Threshold-Severe',
		body : '{ "name": "Processing-Time-Threshold-Severe", "condition": "ts(RAD.metricName.akka.actor.processing-time.*.max) > 1000000", "minutes": 1, "resolveAfterMinutes": 2, "severity": "SEVERE" }'
	}]
}

```


#### MongoDB
Currently in this version of the platform, only the url string is being used to connect to the mongo db instance. If you are connecting to a remote insance instead, you may only change the url property under the mongo.
Without any authentication, a url string looks like this.
```
mongodb://<remote_instance_ip>:<port_number>/<database_name>
```
If you are using authentication, you may use the following format of the url string.
```
mongodb://<username>:<password>@<remote_instance_ip>:<port_number>/<database_name>
```


### Starting up the Platform in cluster Mode(Experimental)
To get more scalibility, node has the ability to run a process in cluster mode. What this means is that while running, it would fork the process and run several instance of the processes. These are called workers. Their is a master which acts as load balancer for the child processes. 

Currently in this version of the RAD-Squared Observability Platform, we are using the pm2 as the process manager. 

Now the thing to note is that this application is currently not optimized that much to run in cluster mode, and there are several bugs that may appear time to time, but this is also your best shot if you say want to work with an RAD-Squared that has say 1 lac actors.
To run it, you need to install pm2 on your machine first. Run the following command.
```
npm install -g pm2
```

After installing pm2, you can run the application in cluster mode by running the following command.
```
npm run cluster
```

If you wish see the logs generated(i mean the console.log statements for now) by the cluster, you may run the following command.
```
npm run cluster-logs
```

And if you wish to shutdown the cluster, them you can simply run
```
npm run shutdown-cluster
```
If everything was set up correctly, then the platform should be running correctly!!. Visit (https://localhost:9425/) to view the run the application and see if it is working. 

### Setting up the RAD-Squared Configs
(Below config changes are under the application.conf file of the rad2-sb module in the root of this repo.)

Now you need to also ask the RAD-Squared instance to send the metrics to the observability platform, for that you need to specify the ip address and the port number of the plaform.
```
kamon.influxdb {
  hostname = "<IP>"
  port = <PORT>

  percentiles = [50.0, 70.0, 90.0, 95.0, 99.0, 99.9]

  additional-tags {

    service = yes
    host = yes
    instance = yes
  }
}
```

Also if you think that there a lot of metrics to process, and it is taking more than the default 10 seconds tick time set by kamon, you may change that by changing this parameter in the congigs.
```
kamon.metric.tick-interval = <X> seconds
```
