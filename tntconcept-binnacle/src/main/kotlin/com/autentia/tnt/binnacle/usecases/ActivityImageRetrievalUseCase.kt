package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.core.utils.toDate
import com.autentia.tnt.binnacle.exception.NoImageInActivityException
import com.autentia.tnt.binnacle.services.ActivityEvidenceService
import com.autentia.tnt.binnacle.services.ActivityService
import jakarta.inject.Singleton

@Singleton
class ActivityImageRetrievalUseCase internal constructor(
    private val activityService: ActivityService,
    private val activityEvidenceService: ActivityEvidenceService,
) {
    fun getActivityImage(id: Long): String {
        val activity = activityService.getActivityById(id)

        if (activity.hasEvidences) {
            return activityEvidenceService.getActivityEvidenceAsBase64String(id, toDate(activity.insertDate)!!)
        } else {
            throw NoImageInActivityException(id)
        }
    }
}
