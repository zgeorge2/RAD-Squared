const BaseMetricParser = require('../basemetricparser');
const config = require('../../config/masterconfig')["kamon-parser"];
const metricsToParse = config.metricsToParse;
const chunkSize = config.asyncChunkSize;
const asyncArrayParse = require('../../common/asyncarrayparse');

function KamonParser() {
	if(!KamonParser.prototype._instance) {
		console.log("Creating new instance for KamonParser");
		KamonParser.prototype._instance = this;
	} else {
		console.log("Using an existing instance for KamonParser");
	}
	
	return KamonParser.prototype._instance;
}

KamonParser.prototype = Object.create(BaseMetricParser.prototype);

KamonParser.prototype.asyncParse = function(rawMetric) {
	return new Promise((resolve, reject) => {
		var finalMetrics = {

		};

		var rawMetricRows = rawMetric.split('\n');
		asyncArrayParse(rawMetricRows,(row) => {
			var metric = this.parseRow(row);

			// Allow Other operations only if metric is to be recorded
			if(metric.metricName) {
				// Push to Array of Metrics
				if(!finalMetrics[metric.metricName]) {
					finalMetrics[metric.metricName] = [];
				}
				finalMetrics[metric.metricName].push(metric);
			}
		}, () => {
			resolve(finalMetrics);
		}, chunkSize);
	});
}

KamonParser.prototype.parse = function(rawMetric) {
	// if(!(rawMetric instanceof String)) {
	// 	// TODO Throw Exception here
	// 	return ;
	// }
	var rawMetricRows = rawMetric.split('\n');
	console.log(rawMetricRows.length);
	return this.parseMetricByRow(rawMetricRows);

};

KamonParser.prototype.parseMetricByRow = function(metricRows) {
	// TODO Error Checking
	var finalMetrics = {

	};
	metricRows.forEach((row) => {
		if(row != "") {
			var metric = this.parseRow(row);
			// Allow Other operations only if metric is to be recorded
			if(metric.metricName) {
				// Push to Array of Metrics
				if(!finalMetrics[metric.metricName]) {
					finalMetrics[metric.metricName] = [];
				}
				finalMetrics[metric.metricName].push(metric);
			}			
		}
	});

	return finalMetrics;
};

KamonParser.prototype.parseRow = function(row) {
	var metric = {

	};

	var rowSplit = row.split(" ");

	if(rowSplit.length != 3) {
		// TODO Throw Exception
	}

	// Get Metric Tags
	metric = this.parseTags(rowSplit[0], metric);

	// Allow Other operations only if metric is to be recorded
	if(metric.metricName) {
		// Get Metric Data
		metric = this.parseData(rowSplit[1], metric);

		// Get Metric TimeStamp
		metric = this.parseTimeStamp(rowSplit[2], metric);
	}

	return metric;
};

KamonParser.prototype.parseTags = function(tags, metric) {
	// TODO Error Checking
	var tagsRow = tags.split(",");

	//Check If metric is to be taken or not
	if(!metricsToParse[tagsRow[0]]) {
		return metric;
	}

	// Add Metric Name
	metric.metricName = tagsRow[0];

	var values;
	for(var i = 1; i < tagsRow.length; i++) {
		values = tagsRow[i].split("=");
		metric[values[0]] = values[1];
	}

	return metric;
}

KamonParser.prototype.parseData = function(data, metric) {
	// TODO Error Checking
	var dataRow = data.split(",");

	var values;
	dataRow.forEach((row) => {
		values = row.split("=");
		// If Value is integer(appended with an i)
		if(values[1][values[1].length-1] === "i") {
			metric[values[0].replace('.', "")]  = Number(values[1].slice(0,values[1].length-1));
		} else {
			metric[values[0].replace('.', "")]  = Number(values[1]);
		}
	});

	return metric;
}

KamonParser.prototype.parseTimeStamp = function(timestamp, metric) {
	// TODO Error Checking
	metric.timestamp = new Date(Number(timestamp)*1000);
	return metric;
}

new KamonParser();

module.exports = KamonParser;