const formatter = require('./formatter');
const WavefrontProxyClient = require('./wavefrontproxyclient');
const WavefrontAlertManger = require('../wavefront-alert-manager');
const config = require('../config/masterconfig').wavefront;

function WavefrontReporter() {
	if(!WavefrontReporter.prototype._instance) {
		console.log("Creating new instance for WavefrontReporter");
		WavefrontReporter.prototype._instance = this;
	} else {
		console.log("Using an existing instance for WavefrontReporter");
	}
	return WavefrontReporter.prototype._instance;
}

WavefrontReporter.prototype.start = function(isMaster) {
	return new Promise((resolve, reject) => {
		if(config.sendMetrics) {
			this.client = new WavefrontProxyClient(config.proxy.port, config.proxy.host);
			this.client.start()
				.then(() => {
					resolve();
				})
				.catch((err) => {
					reject(err);
				});
			if(config.alerting.enable == true && !isMaster) {
				console.log("*************** Master Detected!, Setting up Wavefront Alert Manager!!");
				this.alertManager = new WavefrontAlertManger(config.clusterName, config.apiKey);
				this.alertManager.start();
			}
		}
		else {
			resolve();
		}
	});
}

WavefrontReporter.prototype.report = function(metrics) {
	metrics.forEach((metric) => {
		if(!config.data) {
			console.log('No data to report');
		} else {
			config.data.forEach((d) => {
				if(metric[d] != undefined) {
					var point = config.prefix + "." + formatter.formatMetric(metric, config.tags, d)
					this.client.send(point);
				}
			})
		}
	});
}

new WavefrontReporter();

module.exports = WavefrontReporter;