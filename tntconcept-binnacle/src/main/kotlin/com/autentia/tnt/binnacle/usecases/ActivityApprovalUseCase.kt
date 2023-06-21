package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.services.ApprovedActivityMailService
import com.autentia.tnt.binnacle.services.UserService
import com.autentia.tnt.binnacle.validators.ActivityValidator
import com.autentia.tnt.security.application.checkActivityApprovalRole
import io.micronaut.security.utils.SecurityService
import jakarta.inject.Singleton
import java.util.Locale

@Singleton
class ActivityApprovalUseCase internal constructor(
    private val activityService: ActivityService,
    private val securityService: SecurityService,
    private val activityResponseConverter: ActivityResponseConverter,
    private val userService: UserService,
    private val approvedActivityMailService: ApprovedActivityMailService,
    private val activityValidator: ActivityValidator,
) {
    fun approveActivity(id: Long, locale: Locale): ActivityResponseDTO {
        securityService.checkActivityApprovalRole()
        activityValidator.checkActivityIsValidForApproval(id)
        val activity = activityService.approveActivityById(id)
        if (activity.approvalState == ApprovalState.ACCEPTED) {
            val user = userService.getById(activity.userId)
            approvedActivityMailService.sendApprovedActivityMail(activity, user, locale)
        }

        return activityResponseConverter.toActivityResponseDTO(activity)
    }
}