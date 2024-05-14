package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivityIntervalResponseConverter
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.entities.dto.SubcontractedActivityResponseDTO
import com.autentia.tnt.binnacle.exception.ActivityNotFoundException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import io.archimedesfw.commons.time.ClockUtils
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth

internal class SubcontractedActivityRetrievalByIdUseCaseTest {
    private val activityRepository = mock<ActivityRepository>()


    private val subcontractedActivityRetrievalByIdUseCase =
            SubcontractedActivityRetrievalByIdUseCase(
                    activityRepository,
                    ActivityResponseConverter(
                            ActivityIntervalResponseConverter()
                    )
            )

    @Test
    fun `get subcontracted activity by id`() {
        whenever(activityRepository.findById(activityToRetrieveFromRepository.id!!)).thenReturn(
                activityToRetrieveFromRepository
        )

        val actual = subcontractedActivityRetrievalByIdUseCase.getActivityById(activityToRetrieveFromRepository.id!!)

        Assertions.assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(subcontractedActivityResponseDTO)
    }

    @Test
    fun `fail when the subcontracted activity was not found by id`() {
        assertThrows<ActivityNotFoundException> {
            subcontractedActivityRetrievalByIdUseCase.getActivityById(notFoundActivityId)
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
                        LocalDate.now(),
                        null,
                        null,
                        Organization(1L, "Dummy Organization", 1, listOf()),
                        listOf(),
                        "TIME_AND_MATERIALS"
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
                        LocalDateTime.of(ClockUtils.nowUtc().toLocalDate(), LocalTime.NOON),
                        LocalDateTime.of(ClockUtils.nowUtc().toLocalDate(), LocalTime.NOON).plusMinutes(60)
                ),
                18000,
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

        val DURATION = 18000

        private val activityToRetrieveFromRepository = Activity.of(
                ACTIVITY,
                PROJECT_ROLE
        )

        private val subcontractedActivityResponseDTO = SubcontractedActivityResponseDTO(
                DURATION,
                "Dummy description",
                1L,
                PROJECT_ROLE.id,
                YearMonth.of(ClockUtils.nowUtc().year,ClockUtils.nowUtc().month),
                1L,
        )
    }
}