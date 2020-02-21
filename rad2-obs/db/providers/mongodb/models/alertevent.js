const mongoose = require('mongoose');


var alertEventSchema = new mongoose.Schema({
	name : String,
	nodeId : String,
	timestamp : Date,
	status : String
});

module.exports = mongoose.model('AlertEvent', alertEventSchema);