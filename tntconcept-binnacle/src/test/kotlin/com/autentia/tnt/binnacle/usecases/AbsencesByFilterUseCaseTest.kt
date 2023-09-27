package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createActivity
import com.autentia.tnt.binnacle.converters.AbsenceResponseConverter
import com.autentia.tnt.binnacle.entities.Absence
import com.autentia.tnt.binnacle.entities.AbsenceId
import com.autentia.tnt.binnacle.entities.dto.AbsenceDTO
import com.autentia.tnt.binnacle.entities.dto.AbsenceFilterDTO
import com.autentia.tnt.binnacle.entities.dto.AbsenceType
import com.autentia.tnt.binnacle.repositories.AbsenceRepository
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates
import com.autentia.tnt.binnacle.repositories.predicates.PredicateBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.Month

internal class AbsencesByFilterUseCaseTest {

    private val activityRepository = mock<ActivityRepository>()
    private val absenceRepository = mock<AbsenceRepository>()
    private val absenceResponseConverter = AbsenceResponseConverter()

    private val absencesByFilterUseCase =
        AbsencesByFilterUseCase(activityRepository, absenceRepository, absenceResponseConverter)


    @Test
    fun `should return all the absences of requested user ids`() {
        val startDate = LocalDate.of(2023, Month.SEPTEMBER, 1)
        val endDate = LocalDate.of(2023, Month.SEPTEMBER, 30)
        val userIds = listOf(1L, 3L)
        val absenceFilterDTO = AbsenceFilterDTO(startDate, endDate, userIds, null, null)
        val expectedAbsences = listOf(ABSENCE_01, ABSENCE_02)
        val absences = listOf(
            Absence(
                AbsenceId(1, ABSENCE_01.type.name),
                ABSENCE_01.userId,
                ABSENCE_01.userName,
                ABSENCE_01.startDate,
                ABSENCE_01.endDate
            ),
            Absence(
                AbsenceId(2, ABSENCE_02.type.name),
                ABSENCE_02.userId,
                ABSENCE_02.userName,
                ABSENCE_02.startDate,
                ABSENCE_02.endDate
            ),
        )

        whenever(absenceRepository.find(startDate, endDate, userIds)).thenReturn(absences)

        val result = absencesByFilterUseCase.getAbsences(absenceFilterDTO)

        assertEquals(expectedAbsences, result)
    }

    @Test
    fun `should return all the absences of users who have activities which belong to requested organizations and projects`() {
        val startDate = LocalDate.of(2023, Month.SEPTEMBER, 1)
        val endDate = LocalDate.of(2023, Month.SEPTEMBER, 30)
        val userIds = listOf(1L, 3L)
        val projectIds = listOf(45L, 67L)
        val organizationIds = listOf(30L, 21L)
        val absenceFilterDTO = AbsenceFilterDTO(startDate, endDate, null, organizationIds, projectIds)

        val predicate = PredicateBuilder.and(
            PredicateBuilder.and(
                PredicateBuilder.and(
                    ActivityPredicates.startDateLessThanOrEqualTo(absenceFilterDTO.endDate),
                    ActivityPredicates.endDateGreaterThanOrEqualTo(absenceFilterDTO.startDate)
                ),
                ActivityPredicates.organizationIds(absenceFilterDTO.organizationIds!!)
            ),
            ActivityPredicates.projectIds(absenceFilterDTO.projectIds!!)
        )

        val expectedAbsences = listOf(ABSENCE_01, ABSENCE_02)
        val absences = listOf(
            Absence(
                AbsenceId(1, ABSENCE_01.type.name),
                ABSENCE_01.userId,
                ABSENCE_01.userName,
                ABSENCE_01.startDate,
                ABSENCE_01.endDate
            ),
            Absence(
                AbsenceId(2, ABSENCE_02.type.name),
                ABSENCE_02.userId,
                ABSENCE_02.userName,
                ABSENCE_02.startDate,
                ABSENCE_02.endDate
            ),
        )

        whenever(activityRepository.findAll(predicate)).thenReturn(activities)
        whenever(absenceRepository.find(startDate, endDate, userIds)).thenReturn(absences)

        val result = absencesByFilterUseCase.getAbsences(absenceFilterDTO)

        assertEquals(expectedAbsences, result)
    }

    @Test
    fun `should return all the absences of users who have activities which belong to requested organizations`() {
        val startDate = LocalDate.of(2023, Month.SEPTEMBER, 1)
        val endDate = LocalDate.of(2023, Month.SEPTEMBER, 30)
        val userIds = listOf(1L, 3L)
        val organizationIds = listOf(30L, 21L)
        val absenceFilterDTO = AbsenceFilterDTO(startDate, endDate, null, organizationIds, null)

        val predicate = PredicateBuilder.and(
            PredicateBuilder.and(
                ActivityPredicates.startDateLessThanOrEqualTo(absenceFilterDTO.endDate),
                ActivityPredicates.endDateGreaterThanOrEqualTo(absenceFilterDTO.startDate)
            ),
            ActivityPredicates.organizationIds(absenceFilterDTO.organizationIds!!)
        )

        val expectedAbsences = listOf(ABSENCE_01, ABSENCE_02)
        val absences = listOf(
            Absence(
                AbsenceId(1, ABSENCE_01.type.name),
                ABSENCE_01.userId,
                ABSENCE_01.userName,
                ABSENCE_01.startDate,
                ABSENCE_01.endDate
            ),
            Absence(
                AbsenceId(2, ABSENCE_02.type.name),
                ABSENCE_02.userId,
                ABSENCE_02.userName,
                ABSENCE_02.startDate,
                ABSENCE_02.endDate
            ),
        )

        whenever(activityRepository.findAll(predicate)).thenReturn(activities)
        whenever(absenceRepository.find(startDate, endDate, userIds)).thenReturn(absences)

        val result = absencesByFilterUseCase.getAbsences(absenceFilterDTO)

        assertEquals(expectedAbsences, result)
    }

    private companion object {
        private val START_DATE_01 = LocalDate.of(2023, Month.SEPTEMBER, 5)
        private val END_DATE_01 = LocalDate.of(2023, Month.SEPTEMBER, 10)
        private val ABSENCE_01 = AbsenceDTO(2, "John Doe", AbsenceType.VACATION, START_DATE_01, END_DATE_01)
        private val ABSENCE_02 = AbsenceDTO(
            2,
            "John Doe",
            AbsenceType.PAID_LEAVE,
            LocalDate.of(2023, Month.SEPTEMBER, 11),
            LocalDate.of(2023, Month.SEPTEMBER, 14)
        )
        private val activities = listOf(
            createActivity().copy(userId = 1L, description = "Activity 1"),
            createActivity().copy(userId = 3L, description = "Activity 2"),
            createActivity().copy(userId = 1L, description = "Activity 3"),
            createActivity().copy(userId = 1L, description = "Activity 4"),
        )
    }
}