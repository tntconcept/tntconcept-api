package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.config.createAbsence
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate

class InternalAbsenceRepositoryTest {

    private val absenceDao = mock<AbsenceDao>()

    private val internalAbsenceRepository = InternalAbsenceRepository(absenceDao)

    @Test
    fun `find absences by period and user`() {
        val startDate = LocalDate.of(2023, 9, 1)
        val endDate = LocalDate.of(2023, 9, 30)
        val userId = 1L
        val absences = listOf(
            createAbsence(1L, "PAID_LEAVE")
        )

        whenever(absenceDao.findAllByDateBetweenAndUser(startDate, endDate, userId)).thenReturn(absences)

        val result = internalAbsenceRepository.find(startDate, endDate, listOf(userId))

        assertEquals(absences, result)
    }

    @Test
    fun `find absences by period and users`() {
        val startDate = LocalDate.of(2023, 9, 1)
        val endDate = LocalDate.of(2023, 9, 30)
        val userIds = listOf(1L, 2L)
        val absences = listOf(
            createAbsence(1L, "PAID_LEAVE")
        )

        whenever(absenceDao.findAllByDateBetweenAndUsers(startDate, endDate, userIds)).thenReturn(absences)

        val result = internalAbsenceRepository.find(startDate, endDate, userIds)

        assertEquals(absences, result)
    }

    @Test
    fun `find absences by period and null users`() {
        val startDate = LocalDate.of(2023, 9, 1)
        val endDate = LocalDate.of(2023, 9, 30)
        val userIds = null
        val absences = listOf(
            createAbsence(1L, "PAID_LEAVE")
        )

        whenever(absenceDao.findAllByDateBetweenAndUsers(startDate, endDate, userIds)).thenReturn(absences)

        val result = internalAbsenceRepository.find(startDate, endDate, userIds)

        assertEquals(absences, result)
    }
}