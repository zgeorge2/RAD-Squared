const ingestionService = require('../service/ingestionservice')();

function IngestionController() {
	if(!IngestionController.prototype._instance) {
		console.log("Creating new instance for IngestionController");
		IngestionController.prototype._instance = this;
	} else {
		console.log("Using an existing instance for IngestionController");
	}
	
	return IngestionController.prototype._instance;
}

IngestionController.prototype.ingestMetric = function(req, res, next) {
	try {
		ingestionService.ingestMetric(req.body);
		res.send({status : 200});
	} catch(e) {
		console.log(e);
		res.send(e);
	}
	
};

new IngestionController();

module.exports = IngestionController;