const asyncArrayParse = require('../../common/asyncarrayparse');
const chunkSize = 10000000;
module.exports = {
	//Sync Operations
	getActorGraph : function(data, routerData) {
		// data = this.getAllActors(data);

		if(!data) {
			return {};
		}

		if(Object.entries(data).length === 0 && data.constructor === Object) {
			return {};
		}
		var nodesMap = {

 		}

 		var linksMap = {

		}

		var Graph = {
			nodes : [ ],
			links : [ ],
			metrics : data.metrics
		}

		Graph = this.getActorGraphUtil(data, Graph, nodesMap, linksMap);
		Graph = this.getActorGraphUtil(routerData, Graph, nodesMap, linksMap);

		var currTime = Date.now();
		Graph.systems.forEach((system) => {
				Graph.nodes.push({
					id : system,
					name : system,
					type : 'system',
					lastActive : currTime
				})
				nodesMap[system] = Graph.nodes.length;

				Graph.nodes.push({
					id : system + "/" + 'nosender', 
					name : 'nosender',
					type : 'misc',
					lastActive : currTime
				});
				nodesMap[system + "/" + 'nosender'] = Graph.nodes.length;

				Graph.nodes.push({
					id : system + "/" + 'temp', 
					name : 'temp',
					type : 'misc',
					lastActive : currTime
				});
				nodesMap[system + "/" + 'temp'] = Graph.nodes.length;
		});

		Graph.nodesMap = nodesMap;
		Graph.linksMap = linksMap;
		return Graph;	
	},

	getActorGraphUtil : function(data, Graph, nodesMap, linksMap) {
		var systems = new Set();

		var itr = Graph.nodes.length;
		data.forEach((item) => {
			if(nodesMap[item.path] == undefined && item.path.split('/')[1] == 'user') {
				var splitPath = (item.instance + "/" + item.path).split('/');
				var currActor = splitPath[1];
				systems.add(currActor);
				var prevActor;
				for(var i =2; i < splitPath.length; i++) {
					prevActor = currActor;
					currActor = currActor + "/" + splitPath[i];
					if(nodesMap[currActor] == undefined) {
						nodesMap[currActor] = itr;
						Graph.nodes.push({
							id : currActor,
							name : splitPath[i],
							type : 'actor',
							lastActive : new Date(item.timestamp).getTime()
						});
						itr ++;
					}

					if(linksMap[prevActor + ":?" + currActor] == undefined){
						linksMap[prevActor + ":?" + currActor] = Graph.links.length;
						Graph.links.push({
							from : prevActor,
							to : currActor,
					        type : "parent-child",
					        lastActive : new Date(item.timestamp).getTime()
						});
					}
				}
			}
		});

		Graph.systems = systems;

		return Graph;
	},
	getMessageLinks : function(data, type, Graph) {
		var self = this;
		if(!data) {
			return Graph;
		}
		var totalCount = 0;
		data.forEach(message => {
			totalCount = totalCount + message.count;
		});
		data.forEach(message => {
			var sender = message['instance'] + "/" +message['senderPath'];
			var reciever = message['instance'] + "/" +message['recieverPath'];
			reciever = reciever.split('/').splice(1).join('/');

			if(sender.split('/')[2] == 'temp') {
				sender = sender.split('/')[1] + "/" +'temp';
			} else if(sender.split('/')[2] == 'deadLetters') {
				sender = sender.split('/')[1] + "/" + 'nosender';
			}
			else {
				sender = sender.split('/').splice(1).join('/');
			}
			if(message.count > 0) {
				if(Graph.dynamicLinks.linksMap[sender + ':?' + reciever + ':?' + message.message] == undefined) {
					Graph.dynamicLinks.linksMap[sender + ':?' + reciever + ':?' + message.message] = Graph.dynamicLinks.links.length;
					// Check if from actor was not transient
					// TO DO : Check if actor was present or not!
					if(Graph.nodesMap[sender] == undefined) {
						// Sender must be transient actor or a routee
						Graph = self.addToNodes(Graph, sender);
					}

					if(Graph.nodesMap[reciever] == undefined) {
						// Sender must be transient actor or a routee
						Graph = self.addToNodes(Graph, reciever);
					}
					Graph.dynamicLinks.links.push({
						from : sender,
						to : reciever, 
						count : message.count,	
						message : message.message,
						timestamp : message.timestamp,
						type : type
					});
				} else {
					var pos = Graph.dynamicLinks.linksMap[sender + ':?' + reciever + ':?' + message.message];
					Graph.dynamicLinks.links[pos].count += message.count;
					Graph.dynamicLinks.links[pos].title += message.count;
					Graph.dynamicLinks.links[pos].width = Math.round((Graph.dynamicLinks.links[pos].count/totalCount)*10);
				}
			}
			
			
		});
		return Graph;

	},

	addToNodes(Graph, actor) {
		// Check if actor is a routee
		var tempactorSplit = actor.split('/');
		var actorSplit = tempactorSplit;
		tempactorSplit.splice(-1);
		var parentActor = tempactorSplit.join("/");
		if(Graph.nodesMap[parentActor] == undefined) {
			return Graph;
		}

		// Parent Actor exists
		var parentActorNode = Graph.nodes[Graph.nodesMap[parentActor]];
		if(parentActorNode.type == "router") {
			// Parent is Router, we will include it.
			console.log("Hi");
			console.log("Hi");
			Graph.nodesMap[actor] = Graph.nodes.length;

			Graph.nodes.push({
				id : actor,
				name : actorSplit[actorSplit.length - 1],
				type : 'routee',
				lastActive : new Date(parentActorNode.lastActive).getTime()
			});

			Graph.linksMap[parentActor + ":?" + actor] = Graph.links.length;
			Graph.links.push({
				from : parentActor,
				to : actor,
				type : "parent-child",
				lastActive : new Date(parentActorNode.lastActive).getTime()
			});
		}
		return Graph;
	},

	
	// Async Operations
	asyncGetActorGraph : function(data, routerData) {
		var self = this;
		return new Promise((resolve, reject) => {
			var systen;
			if(!data) {
				resolve({});
			}

			if(Object.entries(data).length === 0 && data.constructor === Object) {
				return {};
			}
			var nodesMap = {

	 		}

	 		var linksMap = {

			}

			var Graph = {
				nodes : [ ],
				links : [ ],
				metrics : data.metrics
			}

			self.asyncGetActorGraphUtil(data, Graph, nodesMap, linksMap, 'actor')
				.then((result) => {
					Graph = result;
					var currTime = Graph.nodes[0].lastActive;
					Graph.systems.forEach((system) => {
							Graph.nodes.push({
								id : system,
								name : system,
								type : 'system',
								lastActive : currTime
							})
							nodesMap[system] = Graph.nodes.length;

							Graph.nodes.push({
								id : system + "/" + 'nosender', 
								name : 'nosender',
								type : 'misc',
								lastActive : currTime
							});
							nodesMap[system + "/" + 'nosender'] = Graph.nodes.length;

							Graph.nodes.push({
								id : system + "/" + 'temp', 
								name : 'temp',
								type : 'misc',
								lastActive : currTime
							});
							nodesMap[system + "/" + 'temp'] = Graph.nodes.length;
					});

					Graph.nodesMap = nodesMap;
					Graph.linksMap = linksMap;

					return self.asyncGetActorGraphUtil(routerData, Graph, nodesMap, linksMap, 'router');
				})
				.then((Graph) => {
					resolve(Graph);	
				})
				.catch((err) => {
					reject();
				});
		});
	},
	asyncGetActorGraphUtil : function(data, Graph, nodesMap, linksMap, type) {
		return new Promise((resolve, reject) => {
			if(data == undefined) {
				resolve(Graph);
			}
			var systems = new Set();

			var itr = Graph.nodes.length;
			// data.forEach();
			asyncArrayParse(data, (item) => {
				if(nodesMap[item.path] == undefined && item.path.split('/')[1] == 'user') {
					var splitPath = (item.instance + "/" + item.path).split('/');
					var currActor = splitPath[1];
					systems.add(currActor);
					var prevActor;
					for(var i =2; i < splitPath.length; i++) {
						prevActor = currActor;
						currActor = currActor + "/" + splitPath[i];
						if(nodesMap[currActor] == undefined) {
							nodesMap[currActor] = itr;
							Graph.nodes.push({
								id : currActor,
								name : splitPath[i],
								type : type,
								lastActive : new Date(item.timestamp).getTime()
							});
							itr ++;
						}

						if(linksMap[prevActor + ":?" + currActor] == undefined){
							linksMap[prevActor + ":?" + currActor] = Graph.links.length;
							Graph.links.push({
								from : prevActor,
								to : currActor,
						        type : "parent-child",
						        lastActive : new Date(item.timestamp).getTime()
							});
						}
					}
				}
			}, () => {
				Graph.systems = systems;
				resolve(Graph);
			}, chunkSize);

		});
	},
	asyncGetMessageLinks : function(data, type, Graph) {
		var self = this;
		return new Promise((resolve, reject) => {
			if(!data) {
				resolve(Graph);
			}
			var totalCount = 0;
			data.forEach(message => {
				totalCount = totalCount + message.count;
			});
			// data.forEach();

			asyncArrayParse(data, (message) => {
				var sender = message['instance'] + "/" +message['senderPath'];
				var reciever = message['instance'] + "/" +message['recieverPath'];
				reciever = reciever.split('/').splice(1).join('/');

				if(sender.split('/')[2] == 'temp') {
					sender = sender.split('/')[1] + "/" +'temp';
				} else if(sender.split('/')[2] == 'deadLetters') {
					sender = sender.split('/')[1] + "/" + 'nosender';
				}
				else {
					sender = sender.split('/').splice(1).join('/');
					console.log(sender);
				}
				if(message.count > 0) {
					if(Graph.dynamicLinks.linksMap[sender + ':?' + reciever + ':?' + message.message] == undefined) {
						Graph.dynamicLinks.linksMap[sender + ':?' + reciever + ':?' + message.message] = Graph.dynamicLinks.links.length;
						// Check if from actor was not transient
						// TO DO : Check if actor was present or not!
						if(Graph.nodesMap[sender] == undefined) {
							// Sender must be transient actor or a routee
							Graph = self.addToNodes(Graph, sender);
						}

						if(Graph.nodesMap[reciever] == undefined) {
							// Sender must be transient actor or a routee
							Graph = self.addToNodes(Graph, reciever);
						}

						Graph.dynamicLinks.links.push({
							from : sender,
							to : reciever, 
							count : message.count,	
							message : message.message,
							timestamp : message.timestamp,
							type : type
						});
					} else {
						var pos = Graph.dynamicLinks.linksMap[sender + ':?' + reciever + ':?' + message.message];
						Graph.dynamicLinks.links[pos].count += message.count;
						Graph.dynamicLinks.links[pos].title += message.count;
						Graph.dynamicLinks.links[pos].width = Math.round((Graph.dynamicLinks.links[pos].count/totalCount)*10);
					}
				}
			}, () => {
				resolve(Graph);
			}, chunkSize)
		});
	}
}