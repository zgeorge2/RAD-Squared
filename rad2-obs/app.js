/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

console.log('INSTANCE : ' + process.env.NODE_APP_INSTANCE + " Starting up!!");
const startTime = Date.now();
const expressServer = require('./server/express');

// Database Providers
const mongoProvider = require('./db/providers/mongodb')();

// Wavefront Reporter
const wavefrontReporter = require('./wavefront-reporter')();

function initializeApp() {
	// Initlizing the Application
	mongoProvider.connectToInstance()
		.then(() => {
			return wavefrontReporter.start(Number(process.env.NODE_APP_INSTANCE));
		})
		.then(() => {
			return expressServer.setUpServer();
		})
		.then((port) => {
			console.log("Express Server Started on port " + String(port) +" in " + String(Date.now() - startTime) + " ms");
		})
		.catch((err) => {
			console.log(err.stack);
			process.emit("SIGINT");
		});

	// Application Shutdown Signal
	process.on("SIGINT", () => {
		console.log('Shutting down node process');
		console.log('Disconnecting from Mongo Instance');
		mongoProvider.disconnect();
		console.log('Shutting Down Express Server');
		expressServer.shutdownServer();
		process.exit(0);
	});
}

initializeApp();