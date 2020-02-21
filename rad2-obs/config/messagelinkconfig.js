module.exports = {
	local : {
		type : "local",
		metricName : "akka.message.processing-time",
		color : "rgba(255, 76, 0, 0.7)"
	},
	remote : {
		type : "remote",
		metricName : "akka.remote.message.processing-time",
		color : "rgba(18, 69, 46, 0.7)"
	},
	dl : {
		type : "dl",
		metricName : "akka.message.dead-letters",
		color : "rgba(0, 0, 0, 0.7)"
	}
}