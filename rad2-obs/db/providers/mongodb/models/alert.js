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