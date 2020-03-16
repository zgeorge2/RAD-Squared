/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

const config = require('../config/masterconfig').wavefront.alerting;
const databaseProvider = require('../db/providers/mongodb')();
const ApiClient = require('./apiclient');
const deparse = require('./deparse');

function WavefrontAlertManager(clusterName, apiKey) {
	console.log('WavefrontAlertManager started');
	this.clusterName = clusterName;
	this.apiKey = apiKey;
};

WavefrontAlertManager.prototype.start = function() {
	var self = this;
	this.apiClient = new ApiClient(this.clusterName, this.apiKey);
	this.initAlerts(config.alerts)
		.then(() => {
			self.interval = setInterval(self.manage.bind(self), config.interval)
		})
		.catch(console.log);
}

WavefrontAlertManager.prototype.manage = function() {
	this.apiClient.getAllAlerts()
		.then((res) => {
			if(res.response && res.response.items) {
				res.response.items.forEach((alert) => {
					alert.failingHostLabelPairs.forEach((failingHostLabelPair) => {
						var alertEvent = {
							name :alert.name,
							timestamp : Date.now(),
							nodeId : deparse(failingHostLabelPair.label),
							status : alert.severity
						};
						databaseProvider.createAlertEvent(alertEvent);
					});
				});
			}
		})
		.catch(console.log);
};

WavefrontAlertManager.prototype.initAlerts = function(alerts) {
	const self = this;
	return new Promise((resolve, reject) => {
		databaseProvider.findAllAlertId()
			.then((ids) => {

				ids.forEach((id) => {
					self.apiClient.deleteAlert(id.id)
				});

				databaseProvider.removeAllAlerts();

				alerts.forEach((alert) => {
					self.apiClient.createAlert(alert.body)
						.then((response) => {
							databaseProvider.createAlert({id : response.response.id, name : alert.name});
						})
						.catch(console.log);
				});
				resolve();
			})
			.catch(console.log);
	});
};

module.exports = WavefrontAlertManager;