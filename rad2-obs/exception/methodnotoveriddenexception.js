class MethodNotOverridenException extends Error {
	constructor() {
		super("Method Not Implemented");
		this.name = this.constructor.name;
		Error.captureStackTrace(this, this.constructor);
	}
}

module.exports = MethodNotOverridenException;