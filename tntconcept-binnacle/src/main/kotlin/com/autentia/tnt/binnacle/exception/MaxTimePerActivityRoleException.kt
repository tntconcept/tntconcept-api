package com.autentia.tnt.binnacle.exception

import com.autentia.tnt.binnacle.entities.TimeUnit

class MaxTimePerActivityRoleException(
    val maxAllowedTime: Int,
    val timeUnit: TimeUnit
) : BinnacleException(
    "Reached max registrable duration limit by activity. Max allowed time: $maxAllowedTime $timeUnit"
)


