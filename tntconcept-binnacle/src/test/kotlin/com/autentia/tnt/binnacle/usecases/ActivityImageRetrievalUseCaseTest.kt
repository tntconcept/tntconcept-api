package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createDomainActivity
import com.autentia.tnt.binnacle.core.utils.toLocalDateTime
import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.exception.NoImageInActivityException
import com.autentia.tnt.binnacle.exception.UserPermissionException
import com.autentia.tnt.binnacle.services.ActivityImageService
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.validators.ActivityValidator
import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.utils.SecurityService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.util.*

internal class ActivityImageRetrievalUseCaseTest {

    private val activityService = mock<ActivityService>()
    private val activityImageService = mock<ActivityImageService>()
    private val securityService = mock<SecurityService>()
    private val activityValidator = mock<ActivityValidator>()

    private val activityImageRetrievalUseCase =
        ActivityImageRetrievalUseCase(
            activityService,
            activityImageService,
            securityService,
            activityValidator
        )

    @Test
    fun `return image in base 64 from service`() {
        whenever(securityService.authentication).thenReturn(Optional.of(authentication))
        whenever(activityService.getActivityById(todayActivity.id!!)).thenReturn(todayActivity)
        whenever(activityValidator.userHasAccess(todayActivity, userId)).thenReturn(true)
        whenever(activityImageService.getActivityImageAsBase64(todayActivity.id!!, TODAY_DATE)).thenReturn(IMAGE)

        assertEquals(IMAGE, activityImageRetrievalUseCase.getActivityImage(1L))
    }

    @Test
    fun `throw UserPermissionException when user can't access the activity`() {
        whenever(securityService.authentication).thenReturn(Optional.of(authentication))
        whenever(activityService.getActivityById(ID)).thenReturn(todayActivity)
        whenever(activityValidator.userHasAccess(todayActivity, userId)).thenReturn(false)

        assertThrows<UserPermissionException> {
            activityImageRetrievalUseCase.getActivityImage(ID)
        }
    }

    @Test
    fun `throw NoImageInActivityException with correct id when activity doesn't have an image`() {
        whenever(securityService.authentication).thenReturn(Optional.of(authentication))
        whenever(activityService.getActivityById(ID)).thenReturn(activityWithoutImage)
        whenever(activityValidator.userHasAccess(activityWithoutImage, userId)).thenReturn(true)

        val exception = assertThrows<NoImageInActivityException> {
            activityImageRetrievalUseCase.getActivityImage(ID)
        }

        assertEquals(exception.id, ID)
    }

    private companion object {
        private val userId = 10L
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

        private val authentication =
            ClientAuthentication(userId.toString(), mapOf("roles" to listOf("user")))
    }
}
