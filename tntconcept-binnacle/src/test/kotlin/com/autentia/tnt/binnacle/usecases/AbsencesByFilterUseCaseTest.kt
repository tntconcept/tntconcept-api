package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createActivity
import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.converters.AbsenceResponseConverter
import com.autentia.tnt.binnacle.entities.Absence
import com.autentia.tnt.binnacle.entities.AbsenceId
import com.autentia.tnt.binnacle.entities.dto.*
import com.autentia.tnt.binnacle.repositories.AbsenceRepository
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.UserRepository
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates
import com.autentia.tnt.binnacle.repositories.predicates.PredicateBuilder
import com.autentia.tnt.binnacle.repositories.predicates.UserPredicates
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.Month

internal class AbsencesByFilterUseCaseTest {

    private val activityRepository = mock<ActivityRepository>()
    private val absenceRepository = mock<AbsenceRepository>()
    private val userRepository = mock<UserRepository>()
    private val absenceResponseConverter = AbsenceResponseConverter()

    private val absencesByFilterUseCase =
        AbsencesByFilterUseCase(activityRepository, absenceRepository, userRepository, absenceResponseConverter)

    @Test
    fun `should return IllegalArgumentException if filter is not valid`() {
        val startDate = LocalDate.of(2023, Month.SEPTEMBER, 1)
        val endDate = LocalDate.of(2023, Month.SEPTEMBER, 30)
        val absenceFilterDTO = AbsenceFilterDTO(startDate, endDate, null, null, null)

        assertThrows<IllegalArgumentException> { absencesByFilterUseCase.getAbsences(absenceFilterDTO) }
    }

    @Test
    fun `should return all the absences of requested user ids`() {
        val startDate = LocalDate.of(2023, Month.SEPTEMBER, 1)
        val endDate = LocalDate.of(2023, Month.SEPTEMBER, 30)
        val userIds = listOf(1L, 3L, 5L)
        val absenceFilterDTO = AbsenceFilterDTO(startDate, endDate, userIds, null, null)
        val absences = listOf(
            Absence(
                AbsenceId(1, AbsenceType.VACATION.name),
                1L,
                START_DATE_01,
                END_DATE_01
            ),
            Absence(
                AbsenceId(2, AbsenceType.PAID_LEAVE.name),
                3L,
                LocalDate.of(2023, Month.SEPTEMBER, 11),
                LocalDate.of(2023, Month.SEPTEMBER, 14)
            ),
        )

        val expectedAbsences2 = listOf(
            AbsenceResponseDTO(
                1L, "John Doe",
                listOf(
                    AbsenceDTO(
                        AbsenceType.VACATION, START_DATE_01,
                        END_DATE_01
                    )
                )
            ),
            AbsenceResponseDTO(
                3L, "John Doe 2",
                listOf(
                    AbsenceDTO(
                        AbsenceType.PAID_LEAVE, LocalDate.of(2023, Month.SEPTEMBER, 11),
                        LocalDate.of(2023, Month.SEPTEMBER, 14)
                    )
                )
            ),
            AbsenceResponseDTO(5L, "John Doe 3", listOf())
        )

        whenever(absenceRepository.find(startDate, endDate, userIds)).thenReturn(absences)
        val pageable = Pageable.unpaged()
        whenever(userRepository.findAll(UserPredicates.fromUserIds(userIds),
            pageable.order("name", Sort.Order.Direction.ASC))).thenReturn(users)

        val result = absencesByFilterUseCase.getAbsences(absenceFilterDTO)

        assertEquals(expectedAbsences2, result)
    }

    @Test
    fun `should return all the absences of users who have activities which belong to requested organizations and projects`() {
        val startDate = LocalDate.of(2023, Month.SEPTEMBER, 1)
        val endDate = LocalDate.of(2023, Month.SEPTEMBER, 30)
        val userIds = listOf(1L, 3L, 5L)
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

        val absences = listOf(
            Absence(
                AbsenceId(1, AbsenceType.VACATION.name),
                1L,
                START_DATE_01,
                END_DATE_01
            ),
            Absence(
                AbsenceId(2, AbsenceType.PAID_LEAVE.name),
                3L,
                LocalDate.of(2023, Month.SEPTEMBER, 11),
                LocalDate.of(2023, Month.SEPTEMBER, 14)
            ),
        )

        val expectedAbsences = listOf(
            AbsenceResponseDTO(
                1L, "John Doe",
                listOf(
                    AbsenceDTO(
                        AbsenceType.VACATION, START_DATE_01,
                        END_DATE_01
                    )
                )
            ),
            AbsenceResponseDTO(
                3L, "John Doe 2",
                listOf(
                    AbsenceDTO(
                        AbsenceType.PAID_LEAVE, LocalDate.of(2023, Month.SEPTEMBER, 11),
                        LocalDate.of(2023, Month.SEPTEMBER, 14)
                    )
                )
            ),
            AbsenceResponseDTO(5L, "John Doe 3", listOf())
        )

        whenever(activityRepository.findAll(predicate)).thenReturn(activities)
        whenever(absenceRepository.find(startDate, endDate, userIds)).thenReturn(absences)
        val pageable = Pageable.unpaged()
        whenever(userRepository.findAll(UserPredicates.fromUserIds(userIds),
            pageable.order("name", Sort.Order.Direction.ASC))).thenReturn(users)

        val result = absencesByFilterUseCase.getAbsences(absenceFilterDTO)

        assertEquals(expectedAbsences, result)
    }

    @Test
    fun `should return all the absences of users who have activities which belong to requested organizations`() {
        val startDate = LocalDate.of(2023, Month.SEPTEMBER, 1)
        val endDate = LocalDate.of(2023, Month.SEPTEMBER, 30)
        val requestedUserIds = listOf(1L, 3L)
        val userIds = listOf(1L)
        val organizationIds = listOf(30L, 21L)
        val absenceFilterDTO = AbsenceFilterDTO(startDate, endDate, requestedUserIds, organizationIds, null)

        val organizationActivities = listOf(
            createActivity().copy(userId = 1L, description = "Activity 1"),
        )

        val predicate = PredicateBuilder.and(
            PredicateBuilder.and(
                ActivityPredicates.startDateLessThanOrEqualTo(absenceFilterDTO.endDate),
                ActivityPredicates.endDateGreaterThanOrEqualTo(absenceFilterDTO.startDate)
            ),
            ActivityPredicates.organizationIds(absenceFilterDTO.organizationIds!!)
        )

        val absences = listOf(
            Absence(
                AbsenceId(1, AbsenceType.VACATION.name),
                1L,
                START_DATE_01,
                END_DATE_01
            ),
        )
        val expectedAbsences = listOf(
            AbsenceResponseDTO(
                1L, "John Doe",
                listOf(
                    AbsenceDTO(
                        AbsenceType.VACATION, START_DATE_01,
                        END_DATE_01
                    )
                )
            ),
        )
        val users = listOf(
            createUser().copy(id = 1L, name = "John Doe"),
        )

        whenever(activityRepository.findAll(predicate)).thenReturn(organizationActivities)
        whenever(absenceRepository.find(startDate, endDate, userIds)).thenReturn(absences)
        val pageable = Pageable.unpaged()
        whenever(userRepository.findAll(UserPredicates.fromUserIds(userIds),
            pageable.order("name", Sort.Order.Direction.ASC))).thenReturn(users)

        val result = absencesByFilterUseCase.getAbsences(absenceFilterDTO)

        assertEquals(expectedAbsences, result)
    }

    private companion object {
        private val START_DATE_01 = LocalDate.of(2023, Month.SEPTEMBER, 5)
        private val END_DATE_01 = LocalDate.of(2023, Month.SEPTEMBER, 10)
        private val activities = listOf(
            createActivity().copy(userId = 1L, description = "Activity 1"),
            createActivity().copy(userId = 3L, description = "Activity 2"),
            createActivity().copy(userId = 1L, description = "Activity 3"),
            createActivity().copy(userId = 1L, description = "Activity 4"),
            createActivity().copy(userId = 5L, description = "Activity 5"),
        )
        private val users = listOf(
            createUser().copy(id = 1L, name = "John Doe"),
            createUser().copy(id = 3L, name = "John Doe 2"),
            createUser().copy(id = 5L, name = "John Doe 3"),
        )
    }
}