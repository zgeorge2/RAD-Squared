const MethodNotOveriddenException = require('../exception/methodnotoveriddenexception');

function BaseMetricParser() {

}

BaseMetricParser.prototype.parse = function(rawMetric) {
	throw new MethodNotOveriddenException();
};

BaseMetricParser.prototype.asyncParse = function(rawMetric) {
	throw new MethodNotOveriddenException();
}

module.exports = BaseMetricParser;