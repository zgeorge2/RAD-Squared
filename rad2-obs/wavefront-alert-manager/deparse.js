module.exports = function(metricName) {
	var pathArray = metricName.split('.path.')[1].split('.');
	return pathArray.splice(0, pathArray.length - 1).join('/');
}