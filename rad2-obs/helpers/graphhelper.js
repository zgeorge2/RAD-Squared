const graphConfig = require('../config/graphconfig');

function GraphHelper() {
	if(!GraphHelper.prototype._instance) {
		console.log("Creating new instance for GraphHelper");
		GraphHelper.prototype._instance = this;
	} else {
		console.log("Using an existing instance for GraphHelper");
	}
	
	return GraphHelper.prototype._instance;
}

GraphHelper.prototype.modelGraph = function(nodes, links) {
	var Graph = {
	};

	var modeledNodes = this.modelNodes(nodes);
	var modeledLinks = this.modelLinks(links);
	Graph.nodes = modeledNodes.nodes;
	Graph.nodesMap = modeledNodes.nodesMap;

	Graph.links = modeledLinks.links;
	Graph.linksMap = modeledLinks.linksMap;

	Graph.clusterId = Array.from(modeledNodes.clusterId);

	return Graph;
}

GraphHelper.prototype.modelNodes = function(nodes) {
	var nodesMap = {

	}
	var clusterId = new Set();
	if(nodes) {
		nodes.forEach(function(node, i) {
			delete node._id;
			delete node.__v;
			node.color = graphConfig.nodes.color[node.type];
			node.cid = node.id.split("/")[0];
			clusterId.add(node.cid);
			// node.status = "INFO";
			nodesMap[node.id] = i;
		});
	}

	return {nodes, nodesMap, clusterId};
}

GraphHelper.prototype.modelLinks = function(links) {
	var linksMap = {

	}

	if(links) {
		links.forEach(function(link, i) {
			delete link._id;
			delete link.__v;
			link.color = graphConfig.links[link.type].color;
			linksMap[link.from + ":?" + link.to] = i;
		});
	}

	return {links, linksMap};
}

GraphHelper.prototype.modelDynamicLink = function(aggregatedLinks, widthThreshold) {
	var linksMap = {

	};

	var links = [];

	var totalCount = 0;
	if(aggregatedLinks) {
		aggregatedLinks.forEach(function(link) {
			totalCount += link.count;
		});

		aggregatedLinks.forEach(function(link, i) {
			delete link._id;
			link.title = link.count;
			link.width = Math.ceil((link.count/totalCount)*graphConfig.links.maxWidth);
			link.color = graphConfig.links[link.type].color;
			link.arrows = "to, middle";
			link.weight = (link.count/totalCount);
			if(link.count/totalCount > widthThreshold) {
				linksMap[link.from + ":?" + link.to] = i;
				links.push(link);
			}
			
		});
	}

	return {links, linksMap};
}

new GraphHelper();

module.exports = GraphHelper;