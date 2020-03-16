/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

const mongoose = require('mongoose');


var nodeScehma = new mongoose.Schema({
	id : {
		type : String, 
		unique : true, 
		required : true
	},
	name : String,
	type : {
		type : String,
		default : 'actor'
	},
	lastActive : Date
});

module.exports = mongoose.model('Node', nodeScehma);