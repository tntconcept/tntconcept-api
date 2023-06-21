package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createActivity
import com.autentia.tnt.binnacle.config.createActivityResponseDTO
import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.exception.InvalidActivityApprovalStateException
import com.autentia.tnt.binnacle.exception.NoEvidenceInActivityException
import com.autentia.tnt.binnacle.services.*
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.services.ApprovedActivityMailService
import com.autentia.tnt.binnacle.validators.ActivityValidator
import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.utils.SecurityService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

internal class ActivityApprovalUseCaseTest {

    private val activityService = mock<ActivityService>()
    private val securityService = mock<SecurityService>()
    private val userService = mock<UserService>()
    private val converter = mock<ActivityResponseConverter>()
    private val approvedActivityMailService = mock<ApprovedActivityMailService>()
    private val activityCalendarService = mock<ActivityCalendarService>()
    private val projectService = mock<ProjectService>()
    private val activityValidator = ActivityValidator(activityService, activityCalendarService, projectService)

    private val activityApprovalUseCase: ActivityApprovalUseCase = ActivityApprovalUseCase(
        activityService, securityService, converter, userService, approvedActivityMailService, activityValidator
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
        val activityToApprove = createActivity(approvalState = ApprovalState.PENDING).copy(hasEvidences = true).toDomain()
        val approvedActivity = activityToApprove.copy(approvalState = ApprovalState.ACCEPTED)
        val activityResponseDTO = createActivityResponseDTO(
            activityId,
            approvedActivity.timeInterval.start,
            approvedActivity.timeInterval.end,
            approvedActivity.hasEvidences,
            approvedActivity.approvalState
        )

        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithActivityApprovalRole))
        whenever(activityService.getActivityById(activityId)).thenReturn(activityToApprove)
        whenever(userService.getById(activityToApprove.userId)).thenReturn(user)
        whenever(activityService.approveActivityById(activityId)).thenReturn(approvedActivity)
        whenever(converter.toActivityResponseDTO(approvedActivity)).thenReturn(activityResponseDTO)

        val result = activityApprovalUseCase.approveActivity(activityId, Locale.ENGLISH)

        assertThat(result).isEqualTo(activityResponseDTO)
        verify(approvedActivityMailService, times(1)).sendApprovedActivityMail(
            approvedActivity,
            user,
            Locale.ENGLISH
        )
    }

    @Test
    fun `approve activity with accepted approval state should throw exception`() {
        val activityId = 1L
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithActivityApprovalRole))
        doReturn(activityWithEvidence.copy(approvalState = ApprovalState.ACCEPTED)).whenever(
            activityService
        )
            .getActivityById(activityId)
        assertThrows<InvalidActivityApprovalStateException> {
            activityApprovalUseCase.approveActivity(activityId, Locale.ENGLISH)
        }
    }

    @Test
    fun `approve activity with not applied approval state should throw exception`() {
        val activityId = 1L
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithActivityApprovalRole))
        doReturn(activityWithEvidence.copy(approvalState = ApprovalState.NA)).whenever(
            activityService
        )
            .getActivityById(activityId)
        assertThrows<InvalidActivityApprovalStateException> {
            activityApprovalUseCase.approveActivity(activityId, Locale.ENGLISH)
        }
    }

    @Test
    fun `approve activity without evidence should throw exception`() {
        val activityId = 1L
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationWithActivityApprovalRole))
        whenever(activityService.getActivityById(activityId)).thenReturn(
            activityWithoutEvidence.copy(approvalState = ApprovalState.PENDING)
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

        private val organization = Organization(1L, "Autentia", emptyList())
        private val project =
            Project(1L, "Back-end developers", true, false, LocalDate.now(), null, null, organization, emptyList())
        private val projectRole =
            ProjectRole(10, "Kotlin developer", RequireEvidence.NO, project, 0, true, false, TimeUnit.MINUTES)
        private val activityWithoutEvidence = com.autentia.tnt.binnacle.core.domain.Activity.of(
            null, TimeInterval.of(
                LocalDateTime.of(LocalDate.now(), LocalTime.NOON),
                LocalDateTime.of(LocalDate.now(), LocalTime.NOON).plusMinutes(60)
            ), 60, "Dummy description", projectRole.toDomain(), 1L, false, 1L, null, false, ApprovalState.NA
        )
        private val activityWithEvidence = com.autentia.tnt.binnacle.core.domain.Activity.of(
            null, TimeInterval.of(
                LocalDateTime.of(LocalDate.now(), LocalTime.NOON),
                LocalDateTime.of(LocalDate.now(), LocalTime.NOON).plusMinutes(120)
            ), 120, "Description...", projectRole.toDomain(), 1L, false, 1L, null, true, ApprovalState.NA
        )
    }
}