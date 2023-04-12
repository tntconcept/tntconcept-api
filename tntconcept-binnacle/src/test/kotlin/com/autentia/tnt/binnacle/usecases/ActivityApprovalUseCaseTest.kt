package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createActivity
import com.autentia.tnt.binnacle.config.createActivityResponseDTO
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.services.ActivityService
import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.utils.SecurityService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.util.*

internal class ActivityApprovalUseCaseTest {

    private val activityService = mock<ActivityService>()
    private val securityService = mock<SecurityService>()
    private val converter = mock<ActivityResponseConverter>()

    private val activityApprovalUseCase : ActivityApprovalUseCase = ActivityApprovalUseCase(activityService, securityService, converter)

    @Test
    fun `should throw Illegal State Exception if user is not authenticated`() {
        whenever(securityService.authentication).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> {
            activityApprovalUseCase.approveActivity(activityId)
        }
    }

    @Test
    fun `should throw Illegal Argument Exception if user is not admin`() {
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithoutAdminRole))

        assertThrows<IllegalStateException> {
            activityApprovalUseCase.approveActivity(activityId)
        }
    }

    @Test
    fun `should approve activity`() {
        val activity = createActivity()
        val activityResponseDTO = createActivityResponseDTO(activityId, activity.start, activity.end, activity.hasEvidences, activity.approvalState)

        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithAdminRole))
        whenever(activityService.approveActivityById(activityId)).thenReturn(activity)
        whenever(converter.mapActivityToActivityResponseDTO(activity)).thenReturn(activityResponseDTO)

        val result = activityApprovalUseCase.approveActivity(activityId)

        assertEquals(activityResponseDTO, result)
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