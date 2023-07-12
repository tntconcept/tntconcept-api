package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.exception.ActivityNotFoundException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.UserRepository
import com.autentia.tnt.binnacle.services.ApprovedActivityMailService
import com.autentia.tnt.binnacle.validators.ActivityValidator
import com.autentia.tnt.security.application.checkActivityApprovalRole
import io.micronaut.security.utils.SecurityService
import jakarta.inject.Singleton
import java.util.*
import javax.transaction.Transactional

@Singleton
class ActivityApprovalUseCase internal constructor(
    private val activityRepository: ActivityRepository,
    private val securityService: SecurityService,
    private val activityResponseConverter: ActivityResponseConverter,
    private val userRepository: UserRepository,
    private val approvedActivityMailService: ApprovedActivityMailService,
    private val activityValidator: ActivityValidator,
) {
    @Transactional(rollbackOn = [Exception::class])
    fun approveActivity(id: Long, locale: Locale): ActivityResponseDTO {
        securityService.checkActivityApprovalRole()
        val activityToApprove = activityRepository.findById(id) ?: throw ActivityNotFoundException(id)
        activityValidator.checkActivityIsValidForApproval(activityToApprove.toDomain())

        val activity = updateActivityState(activityToApprove)

        if (activity.approvalState == ApprovalState.ACCEPTED) {
            val user = userRepository.find(activity.userId) ?: error("User is not found")
            approvedActivityMailService.sendApprovedActivityMail(activity, user, locale)
        }

        return activityResponseConverter.toActivityResponseDTO(activity)
    }

    private fun updateActivityState(activityToApprove: Activity): com.autentia.tnt.binnacle.core.domain.Activity {
        activityToApprove.approvalState = ApprovalState.ACCEPTED
        return activityRepository.update(activityToApprove).toDomain()
    }
}