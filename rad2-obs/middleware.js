/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

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