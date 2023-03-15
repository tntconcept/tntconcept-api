package com.autentia.tnt.binnacle.entities

import java.time.LocalDate

class DateInterval private constructor(val start: LocalDate, val end: LocalDate) {

    companion object {
        fun of(start: LocalDate, end: LocalDate) = DateInterval(start, end)
    }

    fun isOneDay() = start.isEqual(end)
    fun includes(localDate: LocalDate) = !localDate.isBefore(start) && !localDate.isAfter(end)
}