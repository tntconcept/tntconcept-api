package com.autentia.tnt.binnacle.exception

class MaxNextYearRequestVacationException : RuntimeException {
    constructor() : super() { }
    constructor(message: String) : super(message) { }
    constructor(message: String, cause: Throwable) : super(message, cause) { }
}
