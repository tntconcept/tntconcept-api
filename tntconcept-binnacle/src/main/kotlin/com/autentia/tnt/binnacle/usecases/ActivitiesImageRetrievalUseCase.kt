package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.exception.NoImageInActivityException
import com.autentia.tnt.binnacle.exception.UserPermissionException
import com.autentia.tnt.binnacle.services.ActivitiesService
import com.autentia.tnt.binnacle.services.ActivityImageService
import com.autentia.tnt.binnacle.services.UserService
import com.autentia.tnt.binnacle.validators.ActivitiesValidator
import jakarta.inject.Singleton

@Deprecated("Use ActivityImageRetrievalUseCase instead")
@Singleton
class ActivitiesImageRetrievalUseCase internal constructor(
    private val activityService: ActivitiesService,
    private val activityImageService: ActivityImageService,
    private val userService: UserService,
    private val activityValidator: ActivitiesValidator
) {
    fun getActivityImage(id: Long): String {
        val user = userService.getAuthenticatedUser()
        val activity = activityService.getActivityById(id)

        if (activityValidator.userHasAccess(activity, user)) {
            if (activity.hasEvidences) {
                return activityImageService.getActivityImageAsBase64(id, activity.insertDate!!)
            } else {
                throw NoImageInActivityException(id)
            }
        } else {
            throw UserPermissionException()
        }
    }
}