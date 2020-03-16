/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

const databaseProvider = require('../db/providers/mongodb')();
const config = require('../config/masterconfig');
const metricParser = require('../metric-parser/' + config.parsers.metric)();
const graphParser = require('../graph-parser/' + config.parsers.graph)();
const wavefrontReporter = require('../wavefront-reporter')();

function IngestionService() {
	if(!IngestionService.prototype._instance) {
		console.log("Creating new instance for IngestionService");
		IngestionService.prototype._instance = this;
	} else {
		console.log("Using an existing instance for IngestionService");
	}
	
	return IngestionService.prototype._instance;
};

IngestionService.prototype.ingestMetric = function(rawMetrics) {
	var startTime = new Date();
	// Convert raw metrics to objects
	metricParser.asyncParse(rawMetrics)
		.then((resultMetrics) => {
			graphParser.asyncGetGraph(resultMetrics)
				.then((graph) => {
					new Promise((res, rej) => {
						res();
					})
					.then(() => {
						if(graph.nodes) {
							return databaseProvider.updateOrCreateManyNodes(graph.nodes)
						} else {
							return new Promise((res, rej) => {
								res();
							});
						}
					})
					.then(() => {
						if(graph.links) {
							return databaseProvider.updateOrCreateManyFixedLinks(graph.links);
						} else {
							return new Promise((res, rej) => {
						 		res();
							});
						}
					})
					.then(() => {
						if(graph.dynamicLinks && graph.dynamicLinks.links) {
							return databaseProvider.createManyDynamicLinks(graph.dynamicLinks.links);
						} else {
							return new Promise((res, rej) => {
								res();
							});
						}
					})
					.then(() => {
						if(resultMetrics) {
							Object.keys(resultMetrics).forEach((key) => {
								if(config.wavefront.sendMetrics == true) {
									wavefrontReporter.report(resultMetrics[key]);
								}
								return databaseProvider.createManyMetric(resultMetrics[key]);
							})
						} else {
							return new Promise((res, rej) => {
								res();
							});
						}
					});
				})
				.catch((err) => {
					throw err;
				});
		})
		.catch(console.log);
		








	// var resultMetrics = metricParser.parse(rawMetrics);
	// Get all the nodes and links
	// var graph = graphParser.getGraph(resultMetrics);
	// console.log('Parsing took ' + (new Date() - startTime) + " ms");

	// // Save all metrics

	// // Save all nodes
	// if(graph.nodes) {
	// 	databaseProvider.updateOrCreateManyNodes(graph.nodes);
	// }
	

	// // Save all Fixed Links
	// if(graph.links) {
	// 	databaseProvider.updateOrCreateManyFixedLinks(graph.links);
	// }
	

	// // Save all Dynamic Links
	// if(graph.dynamicLinks && graph.dynamicLinks.links) {
	// 	databaseProvider.createManyDynamicLinks(graph.dynamicLinks.links);
	// }

};

new IngestionService();

module.exports = IngestionService;