/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

module.exports = {
	formatMetric : function(metric, tags, data) {
		var point = metric["metricName"] + ".";

		point += data + " " + metric[data] + " " + metric.timestamp.getTime();
		
		point += " source=" + metric["host"]

		tags.forEach((tag) => {
			if(metric[tag]) {
				point += " " + tag + "=" + metric[tag].replace("$", ".") + " ";
			}
		});

		return point;
	}
}