/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

const mongoose = require('mongoose');


var metricSchema = new mongoose.Schema({
	nodeId : String,
	timestamp : Date
}, { strict : false });

module.exports = mongoose.model('Metric', metricSchema);