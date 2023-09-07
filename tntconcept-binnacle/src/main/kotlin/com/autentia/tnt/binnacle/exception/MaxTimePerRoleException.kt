package com.autentia.tnt.binnacle.exception

import com.autentia.tnt.binnacle.entities.TimeUnit


class MaxTimePerRoleException(
    val maxAllowedTime: Double,
    val remainingTime: Double,
    val timeUnit: TimeUnit,
    val year: Int,

    ) : BinnacleException(
    "Reached max registrable time limit by the $year year (remaining $timeUnit for this role: $remainingTime, max allowed: $maxAllowedTime)"
)


