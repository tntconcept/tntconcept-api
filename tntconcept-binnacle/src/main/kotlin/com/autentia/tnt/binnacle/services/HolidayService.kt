package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.entities.Holiday
import com.autentia.tnt.binnacle.repositories.HolidayRepository
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import java.time.LocalDate
import java.time.LocalTime
import javax.transaction.Transactional

@Singleton
@Transactional @ReadOnly
internal class HolidayService(private val holidayRepository: HolidayRepository) {
    fun findAllBetweenDate(startDate: LocalDate, endDate: LocalDate): List<Holiday> {
        val startDateMinHour = startDate.atTime(LocalTime.MIN)
        val endDateMaxHour = endDate.atTime(23, 59, 59)
        return holidayRepository.findAllByDateBetween(startDateMinHour, endDateMaxHour)
    }

}
