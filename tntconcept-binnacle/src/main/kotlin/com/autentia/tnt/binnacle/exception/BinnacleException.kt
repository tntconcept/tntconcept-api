package com.autentia.tnt.binnacle.exception

abstract class BinnacleException(
        message: String,
        cause: Exception?
) : RuntimeException(message, cause) {
    constructor(message: String) : this(message, null)
}
