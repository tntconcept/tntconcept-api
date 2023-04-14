package com.autentia.tnt.binnacle.core.domain

import java.time.LocalDateTime


data class Activity(
    val start: LocalDateTime, val end: LocalDateTime, val projectRole: ProjectRole
) {
    fun getTimeInterval() = TimeInterval.of(start, end)
    fun isOneDay() = start.toLocalDate().isEqual(end.toLocalDate())
    fun getDateInterval() = DateInterval.of(start.toLocalDate(), end.toLocalDate())
}