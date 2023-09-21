package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivityIntervalResponseConverter
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.entities.dto.ApprovalDTO
import com.autentia.tnt.binnacle.entities.dto.IntervalResponseDTO
import com.autentia.tnt.binnacle.exception.ActivityNotFoundException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

internal class ActivityRetrievalByIdUseCaseTest {

    private val activityRepository = mock<ActivityRepository>()


    private val activityRetrievalByIdUseCase =
        ActivityRetrievalByIdUseCase(
            activityRepository,
            ActivityResponseConverter(
                ActivityIntervalResponseConverter()
            )
        )

    @Test
    fun `get activity by id`() {
        whenever(activityRepository.findById(activityToRetrieveFromRepository.id!!)).thenReturn(
            activityToRetrieveFromRepository
        )

        val actual = activityRetrievalByIdUseCase.getActivityById(activityToRetrieveFromRepository.id!!)

        assertThat(actual)
            .usingRecursiveComparison()
            .isEqualTo(activityResponseDTO)
    }

    @Test
    fun `fail when the activity was not found by id`() {
        assertThrows<ActivityNotFoundException> {
            activityRetrievalByIdUseCase.getActivityById(notFoundActivityId)
        }
    }

    private companion object {

        private val PROJECT_ROLE = ProjectRole(
            10L,
            "Dummy Project role",
            RequireEvidence.NO,
            Project(
                1L,
                "Dummy Project",
                true,
                false,
                LocalDate.now(),
                null,
                null,
                Organization(1L, "Dummy Organization", 1, listOf()),
                listOf(),
            ),
            0,
            0,
            true,
            false,
            TimeUnit.MINUTES
        )

        private val ACTIVITY = com.autentia.tnt.binnacle.core.domain.Activity.of(
            1L,
            TimeInterval.of(
            LocalDateTime.of(LocalDate.now(), LocalTime.NOON),
            LocalDateTime.of(LocalDate.now(), LocalTime.NOON).plusMinutes(60)
        ),
            60,
            "Dummy description",
            PROJECT_ROLE.toDomain(),
            1L,
            false,
            1L,
            null,
            false,
            ApprovalState.NA,
            null
        )

        private const val notFoundActivityId = 1L

        private val activityToRetrieveFromRepository = Activity.of(
            ACTIVITY,
            PROJECT_ROLE
        )

        private val activityResponseDTO = ActivityResponseDTO(
            false,
            "Dummy description",
            false,
            1L,
            PROJECT_ROLE.id,
            IntervalResponseDTO(
                LocalDateTime.of(LocalDate.now(), LocalTime.NOON),
                LocalDateTime.of(LocalDate.now(), LocalTime.NOON).plusMinutes(60),
                60,
                TimeUnit.MINUTES
            ),
            1L,
            ApprovalDTO(ApprovalState.NA)
        )
    }
}
