package com.autentia.tnt.binnacle.core.domain

import java.time.LocalDate
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class RequestVacation (
    val id: Long? = null,

    @field:NotNull
    val startDate: LocalDate,

    @field:NotNull
    val endDate: LocalDate,

    @field:Size(max = 1024, message = "Description must not exceed 1024 characters")
    var description: String? = null
){
    fun isDateRangeValid(): Boolean {
        return startDate.isBefore(endDate) || startDate.isEqual(endDate)
    }
}
