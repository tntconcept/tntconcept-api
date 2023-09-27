package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.Absence
import java.time.LocalDate

internal interface AbsenceRepository {

    fun find(startDate: LocalDate, endDate: LocalDate, userIds: List<Long>?): List<Absence>
}