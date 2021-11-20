/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

var factory = {

}

const policyRoot = "./policy/";

let policyName = "staticpolicy";

function PolicyEvaluatorFactory() {
	if(!PolicyEvaluatorFactory.prototype._instance) {
		console.log("Creating new instance for PolicyEvaluatorFactory");
		PolicyEvaluatorFactory.prototype._instance = this;
	} else {
		console.log("Using an existing instance for PolicyEvaluatorFactory");
	}
	
	return PolicyEvaluatorFactory.prototype._instance;
};

PolicyEvaluatorFactory.prototype.evaluatePolicy = function() {
    let policy = this.getPolicy(policyName);

    return policy.evaluate();
};

PolicyEvaluatorFactory.prototype.getPolicy = function(policy) {
    if(factory[policy] == undefined) {
        try {
            let policyPath = `${policyRoot}${policy}`;
            console.log(policyPath);
            require.resolve(policyPath);
            factory[policy] = require(policyPath)();
        } catch(e) {
            console.log(e);
            throw e;
        }
    }
     return factory[policy];
};

new PolicyEvaluatorFactory();

module.exports = PolicyEvaluatorFactory;