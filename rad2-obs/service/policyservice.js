/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

// const databaseProvider = require('../db/providers/mongodb')();
// const config = require('../config/masterconfig');

const policyEvaluatorFactory = require("../policy-evaluator")();
const graphService = require("./graphservice")();

const rad2Client = require("../client/rad2");

function PolicyService() {
	if(!PolicyService.prototype._instance) {
		console.log("Creating new instance for PolicyService");
		PolicyService.prototype._instance = this;
	} else {
		console.log("Using an existing instance for PolicyService");
	}
	
	return PolicyService.prototype._instance;
};

PolicyService.prototype.executePolicy = async function() {
	let policyResult = await policyEvaluatorFactory.evaluatePolicy();
	let currentNSCState = await graphService.getCurrentNscState();


	let flattenedResultMap = {};

	policyResult.forEach(function(result) {
		let resultSplit = result.path.split("/");
		flattenedResultMap[result.path] = {
			finalCount: result.count,
			system: resultSplit[0],
			router: resultSplit[resultSplit.length - 1]
		}
	});

	let finalResult = {
		decreaseRoutees: [],
		increaseRoutees: []
	};

	currentNSCState.forEach((nsc) => {
		let currentPath = nsc.doc.from;
		let currentCount =  nsc.count;

		let newCurrentActor = flattenedResultMap[currentPath];

		if(newCurrentActor == undefined) {
			return;
		}

		let newCount = newCurrentActor.finalCount;

		if(newCount > currentCount) {
			finalResult.increaseRoutees.push(newCurrentActor);
		} else if(newCount < currentCount) {
			finalResult.decreaseRoutees.push(newCurrentActor);
		}
	});

	return finalResult;
};

PolicyService.prototype.applyPolicy = async function() {
	let operationsToApply = await this.executePolicy();

	await rad2Client.updateRoutees(operationsToApply.increaseRoutees);

	await rad2Client.updateRoutees(operationsToApply.decreaseRoutees);

	return;
};

new PolicyService();

module.exports = PolicyService;