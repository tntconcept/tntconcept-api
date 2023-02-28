package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.entities.Holiday

data class HolidayResponse(
    val holidays: List<Holiday>,
    val vacations: List<Vacation>
)
