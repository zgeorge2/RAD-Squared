const mongoose = require('mongoose');
const BaseProvider = require('../../dal');

const nodeSchema = require('./models/node');
const fixedLinkSchema = require('./models/fixedlink');
const dynamicLinkSchema = require('./models/dynamiclink');
const metricSchema = require('./models/metric');
const alertSchema = require('./models/alert');
const alertEventSchema = require('./models/alertevent');

const config = require('../../../config/masterconfig').mongo;

mongoose.set('useNewUrlParser', true);
mongoose.set('useFindAndModify', false);
mongoose.set('useCreateIndex', true);
// mongoose.set('debug', true);

function MongoProvider() {
	if(!MongoProvider.prototype._instance) {
		console.log("Creating a new instance of MongoProvider")
		MongoProvider.prototype._instance = this;
	} else {
		console.log("Using an existing instance for MongoProvider");
	}

	return MongoProvider.prototype._instance;
}

MongoProvider.prototype = Object.create(BaseProvider.prototype);

// Connect to Mongo Instance
MongoProvider.prototype.connectToInstance = function() {
	return new Promise((resolve, reject) => {
		mongoose.connect(config.url, (err) => {
			if(err) {
				reject(err);
			}
			console.log('Connected to Mongo Instance');
			resolve();
		});
	});
}

MongoProvider.prototype.disconnect = function() {
	mongoose.disconnect();
}

// Operations for nodes
MongoProvider.prototype.createNode = async function(entity) {
	try {
		var result = await nodeSchema.create(entity);
		return result;
	} catch(e) {
		throw e;
	}
}

MongoProvider.prototype.updateNode = async function(id, entity) {
	try {
		var result = await nodeSchema.findOneAndUpdate({id}, entity, { upsert : true});
		return result;
	} catch(e) {
		throw e;
	}
}

MongoProvider.prototype.getNode = async function(id) {
	try {
		var result = await nodeSchema.findOne({id}).lean();
		return result;
	} catch(e) {
		throw e;
	}
}

MongoProvider.prototype.getAllNodeWithTime = async function(interval) {
	try {
		var cutoff = new Date();
		cutoff.setTime(cutoff.getTime() - interval);
		var result = await nodeSchema.find({ lastActive : { $gt : cutoff } }).lean();
		return result;
	} catch(e) {
		throw e;
	}
};


MongoProvider.prototype.updateOrCreateManyNodes = async function(entities) {
	var bulk = nodeSchema.collection.initializeUnorderedBulkOp();

	entities.forEach((node) =>{
		node.lastActive = new Date(node.lastActive);
		bulk.find({ id : node.id}).upsert().replaceOne({$set : node});
	});

	try {
		var result = await bulk.execute();
		return result; 	
	} catch(e) {
		throw e;
	}
};

// Operations for Fixed Links
MongoProvider.prototype.createFixedLink = async function(entity) {
	try {
		var result = await fixedLinkSchema.create(entity);
		return result;
	} catch(e) {
		throw e;
	}
};

MongoProvider.prototype.updateFixedLink = async function(from, to, entity) {
	try {
		var result = await fixedLinkSchema.findOneAndUpdate({from, to}, entity, { upsert : true });
		return result;
	} catch(e) {
		throw e;
	}
};

MongoProvider.prototype.getFixedLink = async function(from, to) {
	try {
		var result = await fixedLinkSchema.findOne({from, to});
		return result;
	} catch(e) {
		throw e;
	}
};

MongoProvider.prototype.getAllFixedLinkWithTime = async function(interval) {
	try {
		var cutoff = new Date();
		cutoff.setTime(cutoff.getTime() - interval);
		var result = await fixedLinkSchema.find({ lastActive : { $gt : cutoff } }).lean();
		return result;
	} catch(e) {
		throw e;
	}
};

MongoProvider.prototype.updateOrCreateManyFixedLinks = async function(entities) {
	var bulk = fixedLinkSchema.collection.initializeUnorderedBulkOp();

	entities.forEach((fl) =>{
		fl.lastActive = new Date(fl.lastActive);
		bulk.find({ from : fl.from, to : fl.to}).upsert().replaceOne({$set : fl});
	});

	try {
		var result = await bulk.execute();
		return result; 	
	} catch(e) {
		throw e;
	}
}


// Operations for Dynamic Links
MongoProvider.prototype.createDynamicLink = async function(entity) {
	try {
		var result = await dynamicLinkSchema.create(entity);
		return result;
	} catch(e) {
		throw e;
	}
};

MongoProvider.prototype.getAllDynamicLinkWithTimeAndGroupByFromAndTo = async function(interval, type) {
	try {
		var cutoff = new Date();
		cutoff.setTime(cutoff.getTime() - interval);
		var result = await dynamicLinkSchema.aggregate([{
			$match : {
				timestamp : {
					$gt : cutoff
				},
				type : type
			}
		},
		{
			$group : {
				_id : {
					from : "$from", 
					to : "$to"
				},
				from : {
					$first : "$from"
				},
				to : {
					$first : "$to"
				},
				type : {
					$first : "$type"
				},
				count : {
					$sum : "$count"
				}
			}
		}]);
		return result;
	} catch(e) {
		throw e;
	}
};

MongoProvider.prototype.getAllDynamicLinkWithTimeAndGroupByFromAndToAndMessage = async function(nodeId, interval, type) {
	try {
		var cutoff = new Date();
		cutoff.setTime(cutoff.getTime() - interval);
		var result = await dynamicLinkSchema.aggregate([{
			$match : {
				timestamp : {
					$gt : cutoff
				},
				$or : [{
					from : nodeId
				},{
					to : nodeId
				}]
			}
		},
		{
			$group : {
				_id : {
					from : "$from", 
					to : "$to",
					message : "$message"
				},
				from : {
					$first : "$from"
				},
				to : {
					$first : "$to"
				},
				message : {
					$first : "$message"
				},
				type : {
					$first : "$type"
				},
				count : {
					$sum : "$count"
				}
			}
		}]);
		return result;
	} catch(e) {
		throw e;
	}
};

MongoProvider.prototype.createManyDynamicLinks = function(entities) {
	dynamicLinkSchema.insertMany(entities);
}

// Operations for Metrics
MongoProvider.prototype.createManyMetric = async function(entities) {
	var metricsBatch = [];

	entities.forEach((entity) => {
		if(entity.count > 0 && ((entity.max && entity.max > 0) || !entity.max)) {
			metricsBatch.push(entity);
		}
	})

	try {
		var result = await metricSchema.insertMany(metricsBatch)

		return result;
	} catch(e) {
		throw e;
	}

};

MongoProvider.prototype.updateManyMetric = function(a1, a2) {
	var promises = [];
	metricSchema.find({metricName : "akka.actor.processing-time"}).limit(40000).lean().exec((err, metrics) => {
		metrics.forEach(function(metric) {
			if(metric[a1] && metric[a1][a2]) {
				metric[a1+","+a2] = metric[a1][a2];
				delete metric[a1];
				promises.push(metricSchema.findByIdAndUpdate(metric._id,metric));
				console.log('updating it')
			}
		});
		Promise.all(promises)
			.then(() => console.log('done!'))
	});
}

// Operation for Metrics for alerts
MongoProvider.prototype.createAlertEvent = async function(entity) {
	try {
		var result = await alertEventSchema.create(entity);
		return result;
	} catch(e) {
		throw e;
	}
};

MongoProvider.prototype.getAllDistinctAlertEvent = async function(interval) {
	try {
		var cutoff = new Date();
		cutoff.setTime(cutoff.getTime() - interval);
		var result = await alertEventSchema.aggregate([{
			$match : {
				timestamp : {
					$gt : cutoff
				}
			}
		},
		{
			$group : {
				_id : {
					name : "$name", 
					nodeId : "$nodeId",
					status : "$status"
				},
				name : {
					$first : "$name"
				},
				nodeId : {
					$first : "$nodeId"
				},
				status : {
					$first : "$status"
				},
				count : {
					$sum : 1
				}
			}
		}]);
		return result;
	} catch(e) {
		throw e;
	}
};

// Operations for Alerts
MongoProvider.prototype.createAlert = async function(entity) {
	try {
		var result = await alertSchema.create(entity)
		return result;
	} catch(e) {
		throw e;
	}
}

MongoProvider.prototype.findAlert = async function(alertName) {
	try {
		var result = await alertSchema.findOne({name : alertName});
		return result;
	} catch(e) {
		throw e;
	}
}

MongoProvider.prototype.findAllAlertId = async function() {
	try {
		var result = await alertSchema.find({}, 'id');
		return result;
	} catch(e) {
		throw e;
	}
}

MongoProvider.prototype.removeAllAlerts = async function() {
	try {
		var result = await alertSchema.deleteMany({});
		return result;
	} catch(e) {
		throw e;
	}
}

new MongoProvider();

module.exports = MongoProvider;