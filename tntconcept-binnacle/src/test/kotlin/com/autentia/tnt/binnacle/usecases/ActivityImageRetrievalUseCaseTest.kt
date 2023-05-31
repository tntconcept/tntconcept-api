package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createDomainActivity
import com.autentia.tnt.binnacle.core.utils.toLocalDateTime
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.exception.NoImageInActivityException
import com.autentia.tnt.binnacle.services.ActivityEvidenceService
import com.autentia.tnt.binnacle.services.ActivityService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.util.*

internal class ActivityImageRetrievalUseCaseTest {

    private val activityService = mock<ActivityService>()
    private val activityEvidenceService = mock<ActivityEvidenceService>()

    private val activityImageRetrievalUseCase =
        ActivityImageRetrievalUseCase(
            activityService,
            activityEvidenceService
        )

    @Test
    fun `return image in base 64 from service`() {
        whenever(activityService.getActivityById(todayActivity.id!!)).thenReturn(todayActivity)
        whenever(activityEvidenceService.getActivityEvidenceAsBase64String(todayActivity.id!!, TODAY_DATE)).thenReturn(
            IMAGE
        )

        assertEquals(IMAGE, activityImageRetrievalUseCase.getActivityImage(1L))
    }

    @Test
    fun `throw NoImageInActivityException with correct id when activity doesn't have an image`() {
        whenever(activityService.getActivityById(ID)).thenReturn(activityWithoutImage)

        val exception = assertThrows<NoImageInActivityException> {
            activityImageRetrievalUseCase.getActivityImage(ID)
        }

        assertEquals(exception.id, ID)
    }

    private companion object {
        private val TODAY = LocalDateTime.now()
        private val TODAY_DATE = Date()
        private const val IMAGE = "Image in base 64"
        private const val ID = 2L

        private val ORGANIZATION = Organization(1L, "Dummy Organization", listOf())

        private val PROJECT = Project(
            1L,
            "Dummy Project",
            true,
            false,
            ORGANIZATION,
            listOf(),
        )
        private val PROJECT_ROLE =
            ProjectRole(10L, "Dummy Project role", RequireEvidence.NO, PROJECT, 0, true, false, TimeUnit.MINUTES)

        private val todayActivity = createDomainActivity(
            TODAY,
            TODAY.plusMinutes(60),
            60
        ).copy(projectRole = PROJECT_ROLE.toDomain(), hasEvidences = true, insertDate = toLocalDateTime(TODAY_DATE))

        val activityWithoutImage = createDomainActivity(
            TODAY,
            TODAY.plusMinutes(60),
            60
        ).copy(
            projectRole = PROJECT_ROLE.toDomain(), insertDate = toLocalDateTime(TODAY_DATE)
        )
    }
}
