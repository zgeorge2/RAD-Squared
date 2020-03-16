/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

const mongoose = require('mongoose');


var dynamicLinkSchema = new mongoose.Schema({
	from : {
		type : String, 
		required : true
	},
	to : {
		type : String, 
		required : true
	},
	type : {
		type : String,
		default : 'parent-child'
	},
	timestamp : Date,
	count : Number,
	message : String
});

module.exports = mongoose.model('Dynamiclink', dynamicLinkSchema);