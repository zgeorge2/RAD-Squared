/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

const config = require('../../config/masterconfig')["akka-graph-parser"];
const metricNames = config.metricNames;
const util = require('./util');

function AkkaGraphParser() {
	if(!AkkaGraphParser.prototype._instance) {
		console.log("Creating new instance for AkkaGraphParser");
		AkkaGraphParser.prototype._instance = this;
	} else {
		console.log("Using an existing instance for AkkaGraphParser");
	}
	
	return AkkaGraphParser.prototype._instance;
};

AkkaGraphParser.prototype.getGraph = function(metrics) {
	var Graph = {

	};

	Graph = this.getNodesAndFixedLinks(metrics[metricNames.nodesAndFixedLink]);

	Graph.dynamicLinks = {
		links : [],
		linksMap : {

		}
	};

	Graph = this.getDynamicLinks(metrics[metricNames.dynamicLink.local], "local", Graph);

	Graph = this.getDynamicLinks(metrics[metricNames.dynamicLink.remote], "remote", Graph);

	Graph = this.getDynamicLinks(metrics[metricNames.dynamicLink.deadLetters], "dead-letter", Graph);


	return Graph;
};

AkkaGraphParser.prototype.getNodesAndFixedLinks = function(data) {
	return util.getActorGraph(data);
};

AkkaGraphParser.prototype.getDynamicLinks = function(data, type, result) {
	return util.getMessageLinks(data, type, result);
};


AkkaGraphParser.prototype.asyncGetGraph = function(metrics) {
	return new Promise((resolve, reject) => {
		var Graph = {

		};

		this.asyncGetNodesAndFixedLinks(metrics[metricNames.nodesAndFixedLink], metrics[metricNames.routerNodesAndFixedLink])
			.then((result) => {
				Graph = result;
				Graph.dynamicLinks = {
					links : [],
					linksMap : {

					}
				};

				Graph = this.getDynamicLinks(metrics[metricNames.dynamicLink.local], "local", Graph);

				Graph = this.getDynamicLinks(metrics[metricNames.dynamicLink.remote], "remote", Graph);

				Graph = this.getDynamicLinks(metrics[metricNames.dynamicLink.deadLetters], "dead-letter", Graph);

				resolve(Graph);
			})
			.catch((err) => {
				console.log(err);
			});
	});
};

AkkaGraphParser.prototype.asyncGetNodesAndFixedLinks = function(data, routerData) {
	return new Promise((resolve, reject) => {
		util.asyncGetActorGraph(data, routerData)
			.then((result) => {
				resolve(result);
			})
			.catch((err) => {
				reject(err);
			});
	});
};

AkkaGraphParser.prototype.asyncGetDynamicLinks = function(data, type, result) {
	return new Promise((resolve, reject) => {
		util.asyncGetMessageLinks(data, type, result)
			.then((result) => {
				resolve(result);
			})
			.catch((err) => {
				reject(err);
			});
	});
};

new AkkaGraphParser();

module.exports = AkkaGraphParser;