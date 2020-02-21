const mongoose = require('mongoose');


var metricSchema = new mongoose.Schema({
	nodeId : String,
	timestamp : Date
}, { strict : false });

module.exports = mongoose.model('Metric', metricSchema);