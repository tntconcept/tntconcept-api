package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createActivity
import com.autentia.tnt.binnacle.config.createActivityResponseDTO
import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.exception.InvalidActivityApprovalStateException
import com.autentia.tnt.binnacle.exception.NoEvidenceInActivityException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.services.*
import com.autentia.tnt.binnacle.validators.ActivityValidator
import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.utils.SecurityService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.*

internal class ActivityApprovalUseCaseTest {

    private val activityService = mock<ActivityService>()
    private val activityRepository = mock<ActivityRepository>()
    private val securityService = mock<SecurityService>()
    private val userService = mock<UserService>()
    private val converter = mock<ActivityResponseConverter>()
    private val approvedActivityMailService = mock<ApprovedActivityMailService>()
    private val activityCalendarService = mock<ActivityCalendarService>()
    private val projectService = mock<ProjectService>()
    private val activityValidator = ActivityValidator(activityService, activityCalendarService, projectService)

    private val activityApprovalUseCase: ActivityApprovalUseCase = ActivityApprovalUseCase(
        activityRepository, securityService, converter, userService, approvedActivityMailService, activityValidator
    )

    @Test
    fun `should throw Illegal State Exception if user is not authenticated`() {
        whenever(securityService.authentication).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> {
            activityApprovalUseCase.approveActivity(activityId, Locale.ENGLISH)
        }
    }

    @Test
    fun `should throw Illegal State Exception if user is not admin`() {
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithoutAdminRole))

        assertThrows<IllegalStateException> {
            activityApprovalUseCase.approveActivity(activityId, Locale.ENGLISH)
        }
    }

    @Test
    fun `should approve activity`() {
        val user = createUser()
        val activityToApprove =
            createActivity(approvalState = ApprovalState.PENDING).copy(hasEvidences = true)
        val approvedActivity = activityToApprove.copy(approvalState = ApprovalState.ACCEPTED)
        val activityResponseDTO = createActivityResponseDTO(
            activityId,
            approvedActivity.start,
            approvedActivity.end,
            approvedActivity.hasEvidences,
            approvedActivity.approvalState
        )

        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithActivityApprovalRole))
        whenever(activityRepository.findById(activityId)).thenReturn(activityToApprove)
        whenever(userService.getById(activityToApprove.userId)).thenReturn(user)
        whenever(activityRepository.update(approvedActivity)).thenReturn(approvedActivity)
        whenever(converter.toActivityResponseDTO(approvedActivity.toDomain())).thenReturn(activityResponseDTO)

        val result = activityApprovalUseCase.approveActivity(activityId, Locale.ENGLISH)

        assertThat(result).isEqualTo(activityResponseDTO)
        verify(approvedActivityMailService, times(1)).sendApprovedActivityMail(
            approvedActivity.toDomain(),
            user,
            Locale.ENGLISH
        )
    }

    @Test
    fun `approve activity with accepted approval state should throw exception`() {
        val activityWithApprovedState = createActivity(activityId, ApprovalState.ACCEPTED)
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithActivityApprovalRole))
        whenever(activityRepository.findById(activityId)).thenReturn(activityWithApprovedState)

        assertThrows<InvalidActivityApprovalStateException> {
            activityApprovalUseCase.approveActivity(activityId, Locale.ENGLISH)
        }
    }

    @Test
    fun `approve activity with not applicable approval state should throw exception`() {
        val activityWithNotApplicableState = createActivity(activityId, ApprovalState.NA)
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithActivityApprovalRole))
        whenever(activityRepository.findById(activityId)).thenReturn(activityWithNotApplicableState)

        assertThrows<InvalidActivityApprovalStateException> {
            activityApprovalUseCase.approveActivity(activityId, Locale.ENGLISH)
        }
    }

    @Test
    fun `approve activity without evidence should throw exception`() {
        val activityWithoutEvidence = createActivity(activityId, ApprovalState.PENDING)
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithActivityApprovalRole))
        whenever(activityRepository.findById(activityId)).thenReturn(
            activityWithoutEvidence
        )
        assertThrows<NoEvidenceInActivityException> {
            activityApprovalUseCase.approveActivity(activityId, Locale.ENGLISH)
        }
    }

    private companion object {
        private const val activityId = 1L
        private const val userId = 1L
        private val authenticationWithActivityApprovalRole =
            ClientAuthentication(userId.toString(), mapOf("roles" to listOf("activity-approval")))
        private val authenticationWithoutAdminRole =
            ClientAuthentication(userId.toString(), mapOf("roles" to listOf("user")))
    }
}