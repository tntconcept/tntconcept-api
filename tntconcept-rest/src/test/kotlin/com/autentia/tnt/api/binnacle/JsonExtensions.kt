package com.autentia.tnt.api.binnacle

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun LocalDate.toJson(): String = DateTimeFormatter.ISO_LOCAL_DATE.format(this)

fun LocalDateTime.toJson(): String = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(this)
