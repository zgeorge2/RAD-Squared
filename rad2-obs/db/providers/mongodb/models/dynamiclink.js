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