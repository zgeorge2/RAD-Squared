/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

const mongoose = require('mongoose');


var fixedLinkSchema = new mongoose.Schema({
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
	lastActive : Date,
});
fixedLinkSchema.index({ from : 1, to : 1}, { unique : true })

module.exports = mongoose.model('Fixedlink', fixedLinkSchema);