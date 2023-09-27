package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.Absence
import jakarta.inject.Singleton
import java.time.LocalDate

@Singleton
internal class InternalAbsenceRepository(private val absenceDao: AbsenceDao): AbsenceRepository {

    override fun find(startDate: LocalDate, endDate: LocalDate, userIds: List<Long>?): List<Absence> {
        if(!userIds.isNullOrEmpty()) {
            return if (userIds.size == 1) {
                absenceDao.findAllByDateBetweenAndUser(startDate, endDate, userIds.elementAt(0))
            } else {
                absenceDao.findAllByDateBetweenAndUsers(startDate, endDate, userIds)
            }
        }
        return absenceDao.findAllByDateBetweenAndUsers(startDate, endDate, userIds)
    }
}