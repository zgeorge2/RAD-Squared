const databaseProvider = require('../db/providers/mongodb')();
const helper = require('../helpers/alerthelper')();

function AlertService() {
	if(!AlertService.prototype._instance) {
		console.log("Creating new instance for AlertService");
		AlertService.prototype._instance = this;
	} else {
		console.log("Using an existing instance for AlertService");
	}
	
	return AlertService.prototype._instance;
};

AlertService.prototype.getAlertEvents = function(interval) {
	return new Promise((resolve, reject) => {
		databaseProvider.getAllDistinctAlertEvent(interval)
			.then((result) => {
				resolve(helper.modelAlertEvents(result));
			})
			.catch(reject);
	});
};

new AlertService();

module.exports = AlertService;