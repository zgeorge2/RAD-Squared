var nodes = new vis.DataSet(); 
var edges = new vis.DataSet();

// create a network
var container = document.getElementById('mynetwork');
var updatingGraph = false;

// provide the data in the vis format
var data = {
    nodes: nodes,
    edges: edges
};

var options = {
    interaction:{
        tooltipDelay : 300
    },
    layout: {
        improvedLayout : false
    },
    physics : {
        barnesHut: {
          gravitationalConstant: -40000,
          centralGravity: 0.3,
          springLength: 400,
          springConstant: 0.16,
          damping: 0.09,
          avoidOverlap: 0
        }
    }
    
};

var clusterId;

// initialize your network!
var network;

//Query Arguments for graph query
var dataInterval = 5;
var tickTime = 5000;
var widthThreshold = 0.5;

const isHidden = {
    local : false,
    remote : false,
    "dead-letter" : false
};

const colorMap = {
    "CHECKING" : 'rgba(66, 244, 98, 0.7)',
    "NO_DATA" : 'rgba(65, 214, 244, 0.7)',
    "WARN" : "rgba(255, 242, 0, 0.7)",
    "INFO" : "rgba(66, 244, 98, 0.7)",
    "SEVERE" : "rgba(219, 37, 37, 0.7)"
};

const alertPriority = {
    "INFO" : 0,
    "WARN" : 1,
    "SEVERE" : 2
}


function init() {
    network = new vis.Network(container, data, options);
    addListener();
    updateGraphAfterEveryTick();
}

function buildInfo(nodeId) {
    var node = nodes.get(nodeId);
    if(!node) {
        return;
    }
    $("#actor-name").html(nodeId);

    if(nodeId != 'user' || nodeId != 'system') {
        $("#actor-system").html(node.title);
        buildMessageList(nodeId);
    }
    else {
        $("#actor-system").html("");
        $('.actor-message-list').html("")
    }

    // var alertList = buildAlertList(nodeId);
    // if(alertList != '') {
    //  $(".actor-alert").removeClass('hide');
    //  $(".actor-alert-list").html(alertList);
    // }
    // else {
    //  $(".actor-alert").addClass('hide');
    // }
}

function addListener() {
    // nework.off('click');
    network.on('click', event => {
        var nodeId = event['nodes'][0];
        if(nodeId) {
            if(network.isCluster(nodeId) == true) {
                network.openCluster(nodeId);
            } else {
                buildInfo(nodeId);
                $('.actor-info').removeClass('hide');
            }
        } else {
            $('.actor-info').addClass('hide');
            $('.actor-message-list').addClass('hide')
        }
        
    });
}

function addToNetwork(r) {
    if(r.nodes)
        r.nodes.forEach((n) => {
            var node = nodes.get(n.id);
            if(!node) {
                // console.log('adding node', n)
                nodes.add(n);
            }
            else {
                node.title = n.title;
                // nodes.update(node);
            }
        });
    if(r.links)
        r.links.forEach((l) => {
            var edge = edges.get().filter((edge) => {
                return (edge.from === l.from && edge.to === l.to);
            });
            if(!edge || edge.length == 0) {
                // console.log("adding edge!!", l);
                edges.add(l);
            }
        });
}

function removeFromNetwork(r) {
    var nodesInGraph = nodes.get();
    if(r.nodesMap) {
        // console.log("removing nodes!")
        nodesInGraph.forEach((node) => {
            if(r.nodesMap[node.id] == undefined) {
                nodes.remove(node.id);
            }
        });
    }
    else {
        nodesInGraph.forEach((node) => {
            nodes.remove(node.id);
        });
    }
    
}

function clusterGraph() {
    clusterId.forEach((id) => {
        var clusterOptionsByData = {
              joinCondition:function(childOptions) {
                  return childOptions.cid == id;
              },
              clusterNodeProperties: {id : "System:" + id, borderWidth:3, shape : 'circle', label : id}
          };
          network.cluster(clusterOptionsByData);
    });
}

function updateGraph(graph) {
    clusterId = graph.clusterId;
    removeFromNetwork(graph);
    addToNetwork(graph);
    // clusterGraph();
}

function addMessageLinks(links, linksMap, tracingType) {
    if(links && links.length > 0) {
        var toUpdate = [];
        links.forEach((l) => {
            var edge = edges.get().filter((edge) => {
                return ((edge.from === l.from && edge.to === l.to)
                    && edge.type == tracingType);
            });
            // console.log(edge);
            // console.log(l)
            if(!edge || edge.length == 0) {
                // console.log("adding edge!!", l);
                l.hidden = isHidden[tracingType];
                edges.add(l);
            } else if(edge.length == 1) {
                // Edge already exists, update edge with width and count if different
                if(edge[0].hidden == undefined) {
                    edge[0].hidden = false;
                }
                if(!(edge[0].width == l.width && edge[0].count == l.count && edge[0].hidden == isHidden[tracingType])) {
                    // console.log("sadly updating links", l);
                    // console.log(edge[0]);
                    edge[0].width = l.width;
                    edge[0].title = l.title;
                    edge[0].count = l.count;
                    edge[0].hidden = isHidden[tracingType];
                    toUpdate.push(edge[0]);
                } 
            } else {
                // Somehow multiple edge detected, remove all and add the edge, should happen very very rarly, because of vis only
                edges.remove(edge);
                edges.add(l);
            }
        });
        edges.update(toUpdate);
    }
}

function removeMessageLinks(links, linksMap, tracingType) {
    var edge = edges.get().filter((edge) => {
        return (edge.type == tracingType)
    });
    if(edge){
        edge.forEach((e) => {
            if(linksMap[e.from + ":?" + e.to] == undefined) {
                edges.remove(e);
            }
        })
    }
}

function updateMessageLinks(links,linksMap, tracingType) {
    removeMessageLinks(links, linksMap, tracingType);
    addMessageLinks(links,linksMap, tracingType);
}

function updateAlertsOnGraph(alertEvents, nodeMap) {
    var updated = {

    };
    alertEvents.forEach((alertEvent) => {
        var node = nodes.get(alertEvent.nodeId);
        if(node) {
            var status = node.status;
            if((!status) || (!updated[alertEvent.nodeId]) || (status && alertPriority[status] < alertPriority[alertEvent.status])) {
                node.status = alertEvent.status;
                node.color = colorMap[alertEvent.status];
                updated[alertEvent.nodeId] = true;
                nodes.update(node);
            }
        }
    });

    nodes.forEach((node) => {
        if(nodeMap[node.id] == undefined) {
            if(node.status) {
                node.color = colorMap["INFO"];
                node.status = undefined;
                nodes.update(node);
            }
        }
    });
}

function get(uri) {
    return new Promise((resolve, reject) => {
        $.get(uri, function(r) {
            resolve(r);
        });
    })
}

function buildMessageList(nodeId) {
    var localMessagePromise = get("/api/dynamiclinks/local/message?interval=" + dataInterval + "&nodeId=" + nodeId);

    localMessagePromise
        .then(r => {
            $('.actor-message-list').html(r);
            $('.actor-message-list').removeClass('hide')
        });
}

function updateGraphAfterEveryTick() {
    console.log('updating graph...')
    var graphPromise = get("/api/graph?interval=" + dataInterval);
    var localMessagePromise = get("/api/dynamiclinks/local?interval=" + dataInterval + "&widththreshold=" + widthThreshold);
    var remoteMessagePromise = get("/api/dynamiclinks/remote?interval=" + dataInterval + "&widththreshold=" + widthThreshold);
    var deadMessagePromise = get("/api/dynamiclinks/dead-letter?interval=" + dataInterval + "&widththreshold=" + widthThreshold);
    var alertEventPromise = get("/api/alert/event?interval=" + dataInterval);


    Promise.all([graphPromise, localMessagePromise, remoteMessagePromise, deadMessagePromise, alertEventPromise])
        .then(r => {
            updateGraph(r[0]);
            network.setOptions(options);

            updateMessageLinks(r[1].links, r[1].linksMap, 'local')
            updateMessageLinks(r[2].links, r[2].linksMap, 'remote')
            updateMessageLinks(r[3].links, r[3].linksMap, 'dead-letter')
            updateAlertsOnGraph(r[4].alertEvents, r[4].nodeMap);
        });
}

function updateEdgeWithHiddenTag(type) {
    var hidden = isHidden[type];
    // console.log(hidden);
    var edge = edges.get().filter((e) => {
        return e.type === type;
    });

    var toUpdate = []

    edge.forEach((e) => {
        e.hidden = hidden;
        toUpdate.push(e);
    });
    edges.update(toUpdate);
}

var updateGraphIntervalId = setInterval(updateGraphAfterEveryTick, tickTime);
init();