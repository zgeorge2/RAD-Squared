/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */
const BasePolicy = require('../basepolicy');
const policyResult = require("./static.json");

function StaticPolicy() {
	if(!StaticPolicy.prototype._instance) {
		console.log("Creating new instance for StaticPolicy");
		StaticPolicy.prototype._instance = this;
	} else {
		console.log("Using an existing instance for StaticPolicy");
	}
	
	return StaticPolicy.prototype._instance;
};

StaticPolicy.prototype = Object.create(BasePolicy.prototype);

StaticPolicy.prototype.evaluate = async function() {
    return policyResult;
}

new StaticPolicy();


module.exports = StaticPolicy;