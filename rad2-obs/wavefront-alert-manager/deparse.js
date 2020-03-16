/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

module.exports = function(metricName) {
	var pathArray = metricName.split('.path.')[1].split('.');
	return pathArray.splice(0, pathArray.length - 1).join('/');
}