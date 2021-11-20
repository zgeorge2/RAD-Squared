/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

const MethodNotOveriddenException = require('../exception/methodnotoveriddenexception');

function BasePolicy() {

}

BasePolicy.prototype.evaluate = function() {
    throw new MethodNotOveriddenException();
}


module.exports = BasePolicy;