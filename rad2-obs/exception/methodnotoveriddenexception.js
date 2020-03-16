/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

class MethodNotOverridenException extends Error {
	constructor() {
		super("Method Not Implemented");
		this.name = this.constructor.name;
		Error.captureStackTrace(this, this.constructor);
	}
}

module.exports = MethodNotOverridenException;