package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.exception.NoImageInActivityException
import com.autentia.tnt.binnacle.exception.UserPermissionException
import com.autentia.tnt.binnacle.services.ActivityImageService
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.services.UserService
import com.autentia.tnt.binnacle.validators.ActivityValidator
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.util.Date
import org.junit.jupiter.api.Assertions.assertEquals
import org.mockito.Mockito.doReturn

internal class ActivityImageRetrievalUseCaseTest {

    private val activityService = mock<ActivityService>()
    private val activityImageService = mock<ActivityImageService>()
    private val userService = mock<UserService>()
    private val activityValidator = mock<ActivityValidator>()

    private val activityImageRetrievalUseCase =
        ActivityImageRetrievalUseCase(activityService, activityImageService, userService, activityValidator)


    @Test
    fun `return image in base 64 from service`() {
        doReturn(USER).whenever(userService).getAuthenticatedUser()

        doReturn(todayActivity).whenever(activityService).getActivityById(ID)

        doReturn(true).whenever(activityValidator).userHasAccess(todayActivity, USER)

        doReturn(IMAGE).whenever(activityImageService).getActivityImageAsBase64(ID, TODAY_DATE)

        assertEquals(IMAGE, activityImageRetrievalUseCase.getActivityImage(ID))
    }

    @Test
    fun `throw UserPermissionException when user can't access the activity`() {
        doReturn(USER).whenever(userService).getAuthenticatedUser()

        doReturn(todayActivity).whenever(activityService).getActivityById(ID)

        doReturn(false).whenever(activityValidator).userHasAccess(todayActivity, USER)

        doReturn(todayActivity).whenever(activityService).getActivityById(ID)

        assertThrows<UserPermissionException> {
            activityImageRetrievalUseCase.getActivityImage(ID)
        }
    }

    @Test
    fun `throw NoImageInActivityException with correct id when activity doesn't have an image`() {
        doReturn(USER).whenever(userService).getAuthenticatedUser()

        doReturn(activityWithoutImage).whenever(activityService).getActivityById(ID)

        doReturn(true).whenever(activityValidator).userHasAccess(activityWithoutImage, USER)

        val exception = assertThrows<NoImageInActivityException> {
            activityImageRetrievalUseCase.getActivityImage(ID)
        }

        assertEquals(exception.id, ID)
    }

    private companion object {
        private val USER = createUser()
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
        private val PROJECT_ROLE = ProjectRole(10L, "Dummy Project role", false, PROJECT, 0)

        private val todayActivity = Activity(
            ID,
            TODAY,
            TODAY,
            60,
            "Dummy description",
            PROJECT_ROLE,
            USER.id,
            true,
            null,
            TODAY_DATE,
            true,
        )

        val activityWithoutImage = Activity(
            ID,
            TODAY,
            TODAY,
            60,
            "Dummy description",
            PROJECT_ROLE,
            USER.id,
            true,
            null,
            TODAY_DATE,
            false,
        )
    }
}
