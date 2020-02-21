const alertService = require('../service/alertservice')();

function AlertController() {
	if(!AlertController.prototype._instance) {
		console.log("Creating new instance for AlertController");
		AlertController.prototype._instance = this;
	} else {
		console.log("Using an existing instance for AlertController");
	}
	
	return AlertController.prototype._instance;
};

AlertController.prototype.getAlertEvents = function(req, res, next) {
	alertService.getAlertEvents(req.query.interval*60000)
		.then((result) => {
			res.send(result);
		})
		.catch(console.log);
};

new AlertController();

module.exports = AlertController;