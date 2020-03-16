/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

const mongoose = require('mongoose');


var alertSchema = new mongoose.Schema({
	name : {
		type : String,
		unique : true
	},
	id : {
		type : String
	}
});

module.exports = mongoose.model('Alert', alertSchema);