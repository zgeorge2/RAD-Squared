/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

const mongoose = require('mongoose');


var alertEventSchema = new mongoose.Schema({
	name : String,
	nodeId : String,
	timestamp : Date,
	status : String
});

module.exports = mongoose.model('AlertEvent', alertEventSchema);