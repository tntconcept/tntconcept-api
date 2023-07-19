package com.autentia.tnt.binnacle.services

import jakarta.inject.Singleton
import java.time.LocalDateTime

@Singleton
internal class DateService {

    fun getDateNow(): LocalDateTime = LocalDateTime.now()

}