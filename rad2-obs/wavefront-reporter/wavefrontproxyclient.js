/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

const net = require('net');

function WavefrontProxyClient(port, host) {
	this.port = port;
	this.host = host;
	this.firstConnect = true;
}

var reconnecting = false;

WavefrontProxyClient.prototype.start = function() {
	return new Promise((resolve, reject) => {
		const self = this;
		this.client = new net.Socket();

		this.connect()
			.then(() => {
				resolve();
			})
			.catch((err) => {
				reject(err);
			});

	});
}

WavefrontProxyClient.prototype.connect = function() {
	return new Promise((resolve, reject) => {

		const self = this;

		if(self.firstConnect) {
			// Connect Listener
			this.client.on('connect', () => {
				console.log("Connected To Wavefront Proxy @ " + self.host + ":" + self.port);
				self.firstConnect = false;
				resolve();
			});


			// Error Listener
			self.client.on('error', (err) => {
				console.log("Lost Connection To Wavefront Proxy @ " + self.host + ":" + self.port);
				if(self.firstConnect) {
					self.firstConnect = false;
					reject(err);
				} else {
					if(!reconnecting) {
						reconnecting = true;
						console.log("Reconnecting To Wavefront Proxy @ " + self.host + ":" + self.port);
						self.stop();
						setTimeout(() => {
							reconnecting = false;
							self.connect();
						}, 8000);
					}
				}
			});


		}

		this.client.connect(self.port, self.host);
	});
}


WavefrontProxyClient.prototype.stop = function() {
	this.client.end();
}

WavefrontProxyClient.prototype.send = function(point) {
	if(!reconnecting) {
		this.client.write(point + '\n');
	}
}

module.exports = WavefrontProxyClient;