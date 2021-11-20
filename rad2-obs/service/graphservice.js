/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

const databaseProvider = require('../db/providers/mongodb')();
const helper = require('../helpers/graphhelper')();

function GraphService() {
	if(!GraphService.prototype._instance) {
		console.log("Creating a new instance of GraphService")
		GraphService.prototype._instance = this;
	} else {
		console.log("Using an existing instance for GraphService");
	}

	return GraphService.prototype._instance;
};

GraphService.prototype.getNodesAndFixedLinks = function(interval) {
	return new Promise((resolve, reject) => {
		var dbTransactionPromiseArray = []
		var getNodesPromise = databaseProvider.getAllNodeWithTime(interval);
		var getFixedLinksPromise = databaseProvider.getAllFixedLinkWithTime(interval);
		dbTransactionPromiseArray.push(getNodesPromise);
		dbTransactionPromiseArray.push(getFixedLinksPromise);

		Promise.all(dbTransactionPromiseArray)
			.then((dbTransactionResult) => {
				var nodes = dbTransactionResult[0];
				var links = dbTransactionResult[1];
				resolve(helper.modelGraph(nodes, links));
				// var links = dbTransactionResult[1];

			})
			.catch(reject);
	});
};

GraphService.prototype.getDynamicLinks = function(interval, type, widthThreshold) {
	return new Promise((resolve, reject) => {
		var getDynamicLinkPromise = databaseProvider.getAllDynamicLinkWithTimeAndGroupByFromAndTo(interval, type);

		getDynamicLinkPromise
			.then(result => {
				resolve(helper.modelDynamicLink(result, widthThreshold));
			})
			.catch(reject);
		});
	
}

GraphService.prototype.getDynamicLinksWithMessage = function(interval, nodeId) {
	return new Promise((resolve, reject) => {
		var getDynamicLinkPromise = databaseProvider.getAllDynamicLinkWithTimeAndGroupByFromAndToAndMessage(nodeId, interval);

		getDynamicLinkPromise
			.then(result => {
				resolve(result)
			})
			.catch(reject);
	});
};

GraphService.prototype.getCurrentNscState = async function() {
	try {
		let timestamp = await databaseProvider.getLastFixedLinkTimestamp();
		let secondsBehind = (new Date().getTime() - timestamp.getTime())/1000;
		console.log(`Fetching the actor state from ${timestamp}, which is ${secondsBehind} seconds behind current time`);
		let result = await databaseProvider.getSecondLevelFixedActorLinksAtTimestamp(timestamp);
		return result;
	} catch(e) {
		throw e;
	}
};

new GraphService();

module.exports = GraphService;