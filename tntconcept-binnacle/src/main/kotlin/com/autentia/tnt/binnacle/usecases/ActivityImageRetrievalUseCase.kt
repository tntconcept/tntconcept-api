package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.core.utils.toDate
import com.autentia.tnt.binnacle.exception.NoImageInActivityException
import com.autentia.tnt.binnacle.exception.UserPermissionException
import com.autentia.tnt.binnacle.services.ActivityImageService
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.validators.ActivityValidator
import com.autentia.tnt.security.application.checkAuthentication
import com.autentia.tnt.security.application.id
import io.micronaut.security.utils.SecurityService
import jakarta.inject.Singleton

@Singleton
class ActivityImageRetrievalUseCase internal constructor(
    private val activityService: ActivityService,
    private val activityImageService: ActivityImageService,
    private val securityService: SecurityService,
    private val activityValidator: ActivityValidator
) {
    fun getActivityImage(id: Long): String {
        val authentication = securityService.checkAuthentication()
        val userId = authentication.id()
        val activity = activityService.getActivityById(id)

        if (activityValidator.userHasAccess(activity, userId)) {
            if (activity.hasEvidences) {
                return activityImageService.getActivityImageAsBase64(id, toDate(activity.insertDate)!!)
            } else {
                throw NoImageInActivityException(id)
            }
        } else {
            throw UserPermissionException()
        }
    }
}
