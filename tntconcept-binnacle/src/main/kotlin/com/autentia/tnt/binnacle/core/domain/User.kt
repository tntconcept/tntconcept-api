package com.autentia.tnt.binnacle.core.domain

import java.time.LocalDate

data class User(
    val id: Long,
    val username: String,
    val name: String,
    val departmentId: Long,
    val hiringDate: LocalDate,
    val email: String,
) {
    fun isBeforeHiringDate(startDate: LocalDate) = startDate.isBefore(hiringDate)
}