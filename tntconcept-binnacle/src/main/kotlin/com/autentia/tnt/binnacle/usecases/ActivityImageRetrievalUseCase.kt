package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.exception.NoImageInActivityException
import com.autentia.tnt.binnacle.exception.UserPermissionException
import com.autentia.tnt.binnacle.services.ActivityImageService
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.services.UserService
import com.autentia.tnt.binnacle.validators.ActivityValidator
import jakarta.inject.Singleton

@Singleton
class ActivityImageRetrievalUseCase internal constructor(
    private val activityService: ActivityService,
    private val activityImageService: ActivityImageService,
    private val userService: UserService,
    private val activityValidator: ActivityValidator
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
