/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

const MethodNotOveriddenException = require('../exception/methodnotoveriddenexception');

function BaseProvider() {

}

BaseProvider.prototype.connect = function() {
	throw new MethodNotOveriddenException();
}

BaseProvider.prototype.disconnect = function() {
	throw new MethodNotOveriddenException();
}

/****************************************************************
 *																*
 *							Readers								*
 *																*
 ****************************************************************/

 //***************************Node*******************************
BaseProvider.prototype.getNode = function(nodeId) {
	throw new MethodNotOveriddenException();
};

BaseProvider.prototype.getAllNodeWithTime = function(interval) {
	throw new MethodNotOveriddenException();
};

//*************************Fixed Links****************************
BaseProvider.prototype.getAllFixedLinkWithTime = function(interval) {
	throw new MethodNotOveriddenException();
};

BaseProvider.prototype.getFixedLink = function(from, to) {
	throw new MethodNotOveriddenException();
};

//*************************Dynamic Link***************************
BaseProvider.prototype.getAllDynamicLinkWithTime = function(interval) {
	throw MethodNotOveriddenException();
};

BaseProvider.prototype.getDynamicLink = function(from, to) {
	throw new MethodNotOveriddenException();
};

//************************alert************************************
BaseProvider.prototype.findAlert = async function(alertName) {
	throw new MethodNotOveriddenException();
};

BaseProvider.prototype.findAllAlertId = async function() {
	throw new MethodNotOveriddenException();
};

//********************Alert Event********************************
BaseProvider.prototype.getAllDistinctAlertEvent = async function(entity) {
	throw new MethodNotOveriddenException();
};

/****************************************************************
 *																*
 *							Writers								*
 *																*
 ****************************************************************/

  //***************************Node*******************************
BaseProvider.prototype.createNode = function(entity) {
	throw new MethodNotOveriddenException();
};

BaseProvider.prototype.updateNode = function(nodeId, entity) {
	throw new MethodNotOveriddenException();
};

BaseProvider.prototype.updateOrCreateManyNodes = function(entities) {
	throw new MethodNotOveriddenException();
}


//*************************Fixed Links****************************
BaseProvider.prototype.createFixedLink = function(entity) {
	throw new MethodNotOveriddenException();
};

BaseProvider.prototype.updateFixedLink = function(entity) {
	throw new MethodNotOveriddenException();
};

BaseProvider.prototype.updateOrCreateManyFixedLinks = function(entities) {
	throw new MethodNotOveriddenException();
}

BaseProvider.prototype.getLastFixedLinkTimestam = function() {
	throw new MethodNotOveriddenException();
}

BaseProvider.prototype.getSecondLevelFixedActorLinks = function(timestamp) {
	throw new MethodNotOveriddenException();
}

//*************************Dynamic Link***************************

BaseProvider.prototype.createDynamicLink = function(entity) {
	throw new MethodNotOveriddenException();
};

BaseProvider.prototype.updateDynamicLink = function(entity) {
	throw new MethodNotOveriddenException();
};

BaseProvider.prototype.createManyDynamicLinks = function(entities) {
	throw new MethodNotOveriddenException();
};

BaseProvider.prototype.getAllDynamicLinkWithTimeAndGroupByFromAndTo = function(interval) {
	throw new MethodNotOveriddenException();
}

//***************************Metric*******************************
BaseProvider.prototype.createMetric = function(entity) {
	throw new MethodNotOveriddenException();
};

//**************************Alert********************************
BaseProvider.prototype.createAlert = async function(entity) {
	throw new MethodNotOveriddenException();
};

BaseProvider.prototype.removeAllAlerts = async function() {
	throw new MethodNotOveriddenException();
};

//********************Alert Event********************************
BaseProvider.prototype.createAlertEvent = async function(entity) {
	throw new MethodNotOveriddenException();
};

module.exports = BaseProvider;