package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.exception.NoEvidenceInActivityException
import com.autentia.tnt.binnacle.exception.UserPermissionException
import com.autentia.tnt.binnacle.services.ActivitiesService
import com.autentia.tnt.binnacle.services.ActivityEvidenceService
import com.autentia.tnt.binnacle.services.UserService
import com.autentia.tnt.binnacle.validators.ActivitiesValidator
import jakarta.inject.Singleton

@Deprecated("Use ActivityImageRetrievalUseCase instead")
@Singleton
class ActivitiesImageRetrievalUseCase internal constructor(
    private val activityService: ActivitiesService,
    private val activityEvidenceService: ActivityEvidenceService,
    private val userService: UserService,
    private val activityValidator: ActivitiesValidator
) {
    fun getActivityImage(id: Long): String {
        val user = userService.getAuthenticatedUser()
        val activity = activityService.getActivityById(id)

        if (activityValidator.userHasAccess(activity, user)) {
            if (activity.hasEvidences) {
                return activityEvidenceService.getActivityEvidenceAsBase64String(id, activity.insertDate!!)
            } else {
                throw NoEvidenceInActivityException(id)
            }
        } else {
            throw UserPermissionException()
        }
    }
}