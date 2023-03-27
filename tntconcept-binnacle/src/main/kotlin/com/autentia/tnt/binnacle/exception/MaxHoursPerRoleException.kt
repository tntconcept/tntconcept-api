package com.autentia.tnt.binnacle.exception

class MaxHoursPerRoleException(
    val maxAllowedHours: Double,
    val remainingHours: Double,
    val year: Int

) : BinnacleException(
    "Reached max registrable hours limit by the $year year (remaining hours for this role: $remainingHours, max allowed hours: $maxAllowedHours)"
)


