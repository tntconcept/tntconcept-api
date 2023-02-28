package com.autentia.tnt.binnacle.core.domain

import java.time.LocalDate

data class CreateVacationResponse (
    val startDate: LocalDate,
    val endDate: LocalDate,
    val days: Int,
    val chargeYear: Int)
