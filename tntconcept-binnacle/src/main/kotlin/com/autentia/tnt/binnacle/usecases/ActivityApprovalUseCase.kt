package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.security.application.checkAdminRole
import io.micronaut.security.utils.SecurityService
import jakarta.inject.Singleton

@Singleton
class ActivityApprovalUseCase internal constructor(
    private val activityService: ActivityService,
    private val securityService: SecurityService,
    private val activityResponseConverter: ActivityResponseConverter
) {
    fun approveActivity(id: Long): ActivityResponseDTO {
        securityService.checkAdminRole()
        val activity = activityService.approveActivityById(id)
        return activityResponseConverter.mapActivityToActivityResponseDTO(activity)
    }
}