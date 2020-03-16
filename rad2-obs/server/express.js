/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

const express = require('express');
const app = express();
const bodyParser = require('body-parser');

//Config
const config = require('../config/masterconfig').express;

// Controllers
const graphController = require('../controllers/graphcontroller')();
const ingestionController =  require('../controllers/ingestioncontroller')();
const alertController = require('../controllers/alertcontroller')();

// Middleware
const middleware = require('../middleware');

var server;

var setUpServer = function() {
	return new Promise((resolve, reject) => {
		app.use(bodyParser.json());
		app.use(express.static("public"));
		app.set("view engine","ejs");

		// Common Query Parameters
		app.use(middleware.transformQueryParams);

		// Log Request
		app.use((req, res, next) => {
			console.log(req.method + " " + req.url);
			next();
		});

		// Index Page
		app.get('/', (req, res, next) => {
			res.render('index');
		});

		// REST routes
		app.get('/api/graph', graphController.getNodesAndFixedLinks);

		app.get('/api/dynamiclinks/:type', graphController.getDynamicLinks);

		app.get('/api/dynamiclinks/:type/message', graphController.getDynamicLinksWithMessage);

		app.get('/api/alert/event', alertController.getAlertEvents);

		app.use(bodyParser.text({limit: '100mb'}));
		app.post('/write', ingestionController.ingestMetric);
		server = app.listen(config.port, () => {
			resolve(config.port);
		});

		server.on('error', (err) => {
			reject(err);
		});
	});
};

var shutdownServer = function() {
	server.close();
};

module.exports = {
	setUpServer : setUpServer,
	shutdownServer : shutdownServer
}