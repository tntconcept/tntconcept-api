package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.entities.VacationState
import java.time.LocalDate

data class Vacation(
    var id: Long? = null,
    var observations: String = "",
    var description: String = "",
    val state: VacationState,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val days: List<LocalDate>,
    val chargeYear: LocalDate
) {
    fun isRequestedVacation() = state === VacationState.PENDING || state === VacationState.ACCEPT
}
