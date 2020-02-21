module.exports = {
	transformQueryParams : function(req, res, next) {
		// Actor Path
		// if(req.query.nodeId) {
		// 	req.query.nodeId = req.query.nodeId.split('/').join('\\/').split('.').join('\\.');
		// }
		// Tick Frequency
		if(req.query.interval) {
			req.query.interval = Number(req.query.interval);
		} else {
			req.query.interval = 1;
		}

		// Width Threshold
		if(req.query.widththreshold) {
			req.query.widththreshold = Number(req.query.widththreshold);
		} else {
			req.query.widththreshold = 1;
		}
		next();
	}
}