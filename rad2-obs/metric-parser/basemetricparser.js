/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

const MethodNotOveriddenException = require('../exception/methodnotoveriddenexception');

function BaseMetricParser() {

}

BaseMetricParser.prototype.parse = function(rawMetric) {
	throw new MethodNotOveriddenException();
};

BaseMetricParser.prototype.asyncParse = function(rawMetric) {
	throw new MethodNotOveriddenException();
}

module.exports = BaseMetricParser;