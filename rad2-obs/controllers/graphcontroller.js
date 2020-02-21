const graphService = require('../service/graphservice')();

function GraphController() {
	if(!GraphController.prototype._instance) {
		console.log("Creating new instance for GraphController");
		GraphController.prototype._instance = this;
	} else {
		console.log("Using an existing instance for GraphController");
	}
	
	return GraphController.prototype._instance;
}


GraphController.prototype.getNodesAndFixedLinks = function(req, res, next) {
	graphService.getNodesAndFixedLinks(req.query.interval*60000)
		.then((result) => {
			res.send(result);
		})
		.catch(console.log);
};

GraphController.prototype.getDynamicLinks = function(req, res, next) {
	graphService.getDynamicLinks(req.query.interval*60000, req.params.type, req.query.widththreshold)
		.then(result => {
			res.send(result);
		})
		.catch(console.log);
}

GraphController.prototype.getDynamicLinksWithMessage = function(req, res, next) {
	graphService.getDynamicLinksWithMessage(req.query.interval*60000, req.query.nodeId)
		.then((messages => {
			res.render('message2', {messages : messages, nodeId : req.query.nodeId});
		}))
		.catch(console.log);
}

new GraphController();

module.exports = GraphController;