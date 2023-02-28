package com.autentia.tnt.binnacle.exception

abstract class ResourceNotFoundException(
    message: String,
    cause: Exception?,
    val code: String = "RESOURCE_NOT_FOUND"
) : RuntimeException(message, cause) {

    constructor(message: String) : this(message, null)

}
