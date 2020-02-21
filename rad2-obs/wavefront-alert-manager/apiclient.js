const request = require('request');
const apiRoutes = require('./apiroutes');

function WavefrontApiClient(clusterName, apiKey) {
	console.log('WavefrontApiClient started')
	this.clusterName = clusterName;
	this.apiKey = apiKey;
};

WavefrontApiClient.prototype.getAllAlerts = function() {
	return new Promise((resolve, reject) => {
		request.get({
			url : this.clusterName + apiRoutes.getAllAlerts, 
			headers : {
				'Authorization' : 'Bearer ' + this.apiKey
			}
		}, (e, r, b) => {
			if(!e && r.statusCode == 200) {
				resolve(JSON.parse(b));
			} else {
				if(e) {
					reject(e);
				} else {
					reject(b);
				}
			}
		});
	});
};

WavefrontApiClient.prototype.createAlert = function(body) {
	return new Promise((resolve, reject) => {
		request.post({
			url : this.clusterName + apiRoutes.getAllAlerts, 
			headers : {
				'Authorization' : 'Bearer ' + this.apiKey,
				'content-type' : 'application/json'
			},
			body : body
		}, (e, r, b) => {
			if(!e && r.statusCode == 200) {
				resolve(JSON.parse(b));
			} else {
				if(e) {
					reject(e);
				} else {
					reject(b);
				}
			}
		});
	});
};

WavefrontApiClient.prototype.deleteAlert = function(id) {
	return new Promise((resolve, reject) => {
		request.delete({
			url : this.clusterName + apiRoutes.deleteAlert.split(":id").join(id), 
			headers : {
				'Authorization' : 'Bearer ' + this.apiKey,
				'content-type' : 'application/json'
			}
		}, (e, r, b) => {
			if(!e && r.statusCode == 200) {
				resolve(JSON.parse(b));
			} else {
				if(e) {
					reject(e);
				} else {
					reject(b);
				}
			}
		});
	});
};

module.exports = WavefrontApiClient;