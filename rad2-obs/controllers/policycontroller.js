/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

const policyService = require('../service/policyservice')();

function PolicyController() {
	if(!PolicyController.prototype._instance) {
		console.log("Creating new instance for PolicyController");
		PolicyController.prototype._instance = this;
	} else {
		console.log("Using an existing instance for PolicyController");
	}
	
	return PolicyController.prototype._instance;
}

PolicyController.prototype.executePolicy = function(req, res, next) {
	policyService.executePolicy()
		.then(result => {
			res.send(result);
		})
		.catch(err => {
			console.log(err);
			res.send(err);
		});
};

PolicyController.prototype.applyPolicy = async function(req, res, next) {
	policyService.applyPolicy()
		.then((result) => {
			res.send("Policy Applied!");
		})
		.catch((err) => {
			console.log(err);
			res.send(err);
		});
};

new PolicyController();

module.exports = PolicyController;