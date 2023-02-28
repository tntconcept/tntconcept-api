package com.autentia.tnt.binnacle.utils

enum class FailureReason {
    FORBIDDEN,
    ILLEGAL_ARGUMENT,
    ACTIVITY_TIME_OVERLAPS,
    CLOSED_PROJECT
}

open class BinnacleApiIllegalArgumentException(
    message: String?,
    var code: String = FailureReason.ILLEGAL_ARGUMENT.toString()
) : IllegalArgumentException(message)
