function AlertHelper() {
	if(!AlertHelper.prototype._instance) {
		console.log("Creating new instance for AlertHelper");
		AlertHelper.prototype._instance = this;
	} else {
		console.log("Using an existing instance for AlertHelper");
	}
	
	return AlertHelper.prototype._instance;
};

AlertHelper.prototype.modelAlertEvents = function(alertEvents) {
	var nodeMap = {

	};

	alertEvents.forEach((alertEvent, i) => {
		delete alertEvent._id;
		nodeMap[alertEvent.nodeId] = i;
	});

	return {alertEvents, nodeMap};
};

new AlertHelper();

module.exports = AlertHelper;