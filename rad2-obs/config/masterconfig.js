/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

module.exports = {
	express : {
		port : 9425
	},
	wavefront : {
		sendMetrics : false,
		alerting : {
			enable : false,
			interval : 10000,
			alerts : [{
				name : 'Processing-Time-Threshold-Warn',
				body : '{ "name": "Processing-Time-Threshold-Warn", "condition": "ts(RAD.metricName.akka.actor.processing-time.*.max) > 100000", "minutes": 1, "resolveAfterMinutes": 2, "severity": "WARN" }'
			},
			{
				name : 'Processing-Time-Threshold-Severe',
				body : '{ "name": "Processing-Time-Threshold-Severe", "condition": "ts(RAD.metricName.akka.actor.processing-time.*.max) > 1000000", "minutes": 1, "resolveAfterMinutes": 2, "severity": "SEVERE" }'
			}]
		},
		prefix : "RAD",
		tags : ['system', 'class', 'path', 'recieverPath', 'recieverSystem', 'routerClass', 'message'],
		data : ['sum', 'min', 'max', 'sum', 'count', 'p500', 'p700', 'p950', 'p990', 'p999'],
		proxy : {
			host : 'localhost',
			port : 2878
		},
		noderef : "path",
		clusterName : "",
		apiKey : ""
	},
	mongo : {
		url : 'mongodb://localhost:27017/rad_obs_test',
		host : 'localhost',
		port : 27017,
		dbName : 'rad_obs_test',
		username : "",
		password : ""
	},
	dbProviders : "mongo",
	parsers : {
		metric : 'kamon-parser',
		graph : 'akka-graph-parser'
	},
	"kamon-parser" : {
		metricsToParse : {
			"akka.actor.processing-time" : true,
			"akka.actor.mailbox-size" : true,
			"akka.actor.time-in-mailbox" : true,
			"akka.remote.message.processing-time" : true,
			"akka.message.processing-time" : true,
			"akka.message.dead-letters" : true,
			"akka.router.processing-time" : true
		},
		asyncChunkSize : 100
	},
	"akka-graph-parser" : {
		metricNames : {
			nodesAndFixedLink : "akka.actor.processing-time",
			routerNodesAndFixedLink : "akka.router.processing-time",
			dynamicLink : {
				local : "akka.message.processing-time",
				remote : "akka.remote.message.processing-time",
				deadLetters : "akka.message.dead-letters"
			}
		},
		asyncChunkSize : 1000
	}
}