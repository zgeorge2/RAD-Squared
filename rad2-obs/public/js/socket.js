const socket = io.connect('/');

var alertMap = {

}

function buildAlertList(nodeId) {
	var node = nodes.get(nodeId); 	
	if(!node.alerts) {
		return "";
	}
	var result = "";
	node.alerts.forEach((alert) => {
		result = result + "<li>" + alertMap[alert].metric + "-" + alertMap[alert].system + "-<strong>" + alertMap[alert].status[0] + "</strong></li>"
	});

	return result;
}

function setColor(nodeId, color) {
	var node = nodes.get(nodeId);
	if(node) {
		node.color = color;
		nodes.update(node);
	}
}

function pushAlertId(nodeId, alert) {
	var node = nodes.get(nodeId);
	if(node) {
		if(!node.alerts) {
			node.alerts = new Set();
		}
		node.alerts.add(alert.alertId);
		alertMap[alert.alertId] = alert;
		nodes.update(node);
	}
}

socket.on('alerts', (data) => {
	if(data && data.alerts && data.alerts.length > 0) {
		var allreadyAlerted = {

		};
		data.alerts.forEach((alert) => {
			var nodeId = alert.nodeId;
			// console.log(alert.severity)
			if(!allreadyAlerted[nodeId]) {

				if(alert.status[0] == "FIRING") {
					allreadyAlerted[nodeId] = true;

					setColor(nodeId, colorMap[alert.severity]);
				} else {
					setColor(nodeId, colorMap[alert.status[0]]);
				}
			}
			
			pushAlertId(nodeId, alert);
		});
	}
});

socket.on('change-state', (data) => {
	var nodeId = data.id.split('\\/').join('/');
	console.log(nodeId);
	var node = nodes.get(nodeId);
	node.color = data.color;
	nodes.update(node);
});