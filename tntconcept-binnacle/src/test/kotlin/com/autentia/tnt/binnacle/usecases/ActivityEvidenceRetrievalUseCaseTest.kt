package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.core.utils.toDate
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.entities.dto.EvidenceDTO
import com.autentia.tnt.binnacle.exception.NoEvidenceInActivityException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.services.ActivityEvidenceService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

@TestInstance(Lifecycle.PER_CLASS)
internal class ActivityEvidenceRetrievalUseCaseTest {

    private val activityRepository = mock<ActivityRepository>()
    private val activityEvidenceService = mock<ActivityEvidenceService>()

    private val activityEvidenceRetrievalUseCase =
        ActivityEvidenceRetrievalUseCase(
            activityRepository,
            activityEvidenceService
        )

    @AfterEach
    fun resetMocks() {
        reset(activityRepository, activityEvidenceService)
    }

    @Test
    fun `return image in base 64 from service`() {
        whenever(activityRepository.findById(activityWithEvidenceEntity.id!!)).thenReturn(activityWithEvidenceEntity)
        whenever(
            activityEvidenceService.getActivityEvidence(
                activityWithEvidenceEntity.id!!,
                toDate(activityWithEvidenceEntity.toDomain().insertDate)!!
            )
        ).thenReturn(
            IMAGE
        )

        assertEquals(IMAGE, activityEvidenceRetrievalUseCase.getActivityEvidenceByActivityId(ID))
    }

    @Test
    fun `throw NoImageInActivityException with correct id when activity doesn't have an image`() {
        whenever(activityRepository.findById(ID)).thenReturn(activityWithoutEvidenceEntity)

        val exception = assertThrows<NoEvidenceInActivityException> {
            activityEvidenceRetrievalUseCase.getActivityEvidenceByActivityId(ID)
        }
        
        assertEquals(exception.id, ID)
    }

    private companion object {
        private const val ID = 1L
        private val TODAY = LocalDateTime.now()
        private val IMAGE = EvidenceDTO("Image in base 64", "")
        private val organization = Organization(1L, "Autentia", emptyList())
        private val project =
            Project(1L, "Back-end developers", true, false, LocalDate.now(), null, null, organization, emptyList())
        private val projectRole =
            ProjectRole(10, "Kotlin developer", RequireEvidence.NO, project, 0, true, false, TimeUnit.MINUTES)

        private val activityWithEvidence = com.autentia.tnt.binnacle.core.domain.Activity.of(
            1L, TimeInterval.of(
            LocalDateTime.of(LocalDate.now(), LocalTime.NOON),
            LocalDateTime.of(LocalDate.now(), LocalTime.NOON).plusMinutes(120)
        ), 120, "Description...", projectRole.toDomain(), 1L, false, 1L, TODAY, true, ApprovalState.NA, null
        )

        private val activityWithoutEvidence = com.autentia.tnt.binnacle.core.domain.Activity.of(
            1L, TimeInterval.of(
            LocalDateTime.of(LocalDate.now(), LocalTime.NOON),
            LocalDateTime.of(LocalDate.now(), LocalTime.NOON).plusMinutes(120)
        ), 120, "Description...", projectRole.toDomain(), 1L, false, 1L, TODAY, false, ApprovalState.NA, null
        )

        private val activityWithEvidenceEntity = Activity.of(
            activityWithEvidence,
            projectRole
        )

        private val activityWithoutEvidenceEntity = Activity.of(
            activityWithoutEvidence,
            projectRole
        )
    }
}
