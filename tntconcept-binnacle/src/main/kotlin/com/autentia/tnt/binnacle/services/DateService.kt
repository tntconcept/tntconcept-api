package com.autentia.tnt.binnacle.services

import jakarta.inject.Singleton
import java.time.LocalDate
import java.time.LocalDateTime

@Singleton
internal class DateService {

    fun getDateNow(): LocalDateTime = LocalDateTime.now()

    fun getLocalDateNow(): LocalDate = LocalDate.now()

}