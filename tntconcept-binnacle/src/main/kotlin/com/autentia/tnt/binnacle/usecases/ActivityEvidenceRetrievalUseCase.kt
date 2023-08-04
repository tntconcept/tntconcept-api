package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.core.utils.toDate
import com.autentia.tnt.binnacle.entities.dto.EvidenceDTO
import com.autentia.tnt.binnacle.exception.ActivityNotFoundException
import com.autentia.tnt.binnacle.exception.NoEvidenceInActivityException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.services.ActivityEvidenceService
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
class ActivityEvidenceRetrievalUseCase internal constructor(
    private val activityRepository: ActivityRepository,
    private val activityEvidenceService: ActivityEvidenceService,
) {

    @Transactional
    @ReadOnly
    fun getActivityEvidenceByActivityId(activityId: Long): EvidenceDTO {
        val activity = activityRepository.findById(activityId)?.toDomain() ?: throw ActivityNotFoundException(activityId)

        if (activity.hasEvidences) {
            return activityEvidenceService.getActivityEvidence(activityId, toDate(activity.insertDate)!!)
        } else {
            throw NoEvidenceInActivityException(activityId)
        }
    }

}
