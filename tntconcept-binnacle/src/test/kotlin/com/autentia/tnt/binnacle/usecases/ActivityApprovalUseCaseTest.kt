package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createActivity
import com.autentia.tnt.binnacle.config.createActivityResponseDTO
import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.services.ApprovedActivityMailService
import com.autentia.tnt.binnacle.services.UserService
import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.utils.SecurityService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Locale
import java.util.Optional

internal class ActivityApprovalUseCaseTest {

    private val activityService = mock<ActivityService>()
    private val securityService = mock<SecurityService>()
    private val userService = mock<UserService>()
    private val converter = mock<ActivityResponseConverter>()
    private val approvedActivityMailService = mock<ApprovedActivityMailService>()

    private val activityApprovalUseCase : ActivityApprovalUseCase = ActivityApprovalUseCase(
        activityService, securityService, converter, userService, approvedActivityMailService)

    @Test
    fun `should throw Illegal State Exception if user is not authenticated`() {
        whenever(securityService.authentication).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> {
            activityApprovalUseCase.approveActivity(activityId, Locale.ENGLISH)
        }
    }

    @Test
    fun `should throw Illegal Argument Exception if user is not admin`() {
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithoutAdminRole))

        assertThrows<IllegalStateException> {
            activityApprovalUseCase.approveActivity(activityId, Locale.ENGLISH)
        }
    }

    @Test
    fun `should approve activity`() {
        val user = createUser()
        val activity = createActivity(approvalState = ApprovalState.ACCEPTED)
        val activityResponseDTO = createActivityResponseDTO(
            activityId,
            activity.start,
            activity.end,
            activity.hasEvidences,
            activity.approvalState
        )

        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithAdminRole))
        whenever(userService.getAuthenticatedUser()).thenReturn(user)
        whenever(activityService.approveActivityById(activityId)).thenReturn(activity)
        whenever(converter.mapActivityToActivityResponseDTO(activity)).thenReturn(activityResponseDTO)

        val result = activityApprovalUseCase.approveActivity(activityId, Locale.ENGLISH)

        assertEquals(activityResponseDTO, result)
        verify(approvedActivityMailService, times(1)).sendApprovedActivityMail(
            activity,
            user,
            Locale.ENGLISH
        )
    }

    private companion object{
        private const val activityId = 1L
        private const val userId = 1L
        private val authenticationWithAdminRole =
            ClientAuthentication(userId.toString(), mapOf("roles" to listOf("admin")))
        private val authenticationWithoutAdminRole =
            ClientAuthentication(userId.toString(), mapOf("roles" to listOf("user")))
    }
}