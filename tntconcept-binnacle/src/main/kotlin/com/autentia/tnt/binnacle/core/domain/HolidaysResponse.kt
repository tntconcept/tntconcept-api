package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.entities.Holiday

@Deprecated("Use HolidayResponse instead")
data class HolidaysResponse(
    val holidays: List<Holiday>,
    val vacations: List<Vacation>
)
