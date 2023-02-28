package com.autentia.tnt.binnacle.exception

class MaxHoursPerRoleException(
    val maxAllowedHours: Double,
    val remainingHours: Double

) : BinnacleException(
    "Reached max registrable hours limit (remaining hours for this role: $remainingHours, max allowed hours: $maxAllowedHours)"
)


