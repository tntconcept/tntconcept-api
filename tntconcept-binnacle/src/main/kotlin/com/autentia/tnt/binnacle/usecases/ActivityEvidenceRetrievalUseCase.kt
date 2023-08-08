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
    @Deprecated("Use getActivityEvidenceByActivityId")
    @Transactional
    @ReadOnly
    fun getActivityEvidence(id: Long): String {
        val activity = activityRepository.findById(id)?.toDomain() ?: throw ActivityNotFoundException(id)

        if (activity.hasEvidences) {
            return activityEvidenceService.getActivityEvidenceAsBase64String(id, toDate(activity.insertDate)!!)
        } else {
            throw NoEvidenceInActivityException(id)
        }
    }

    @Transactional
    @ReadOnly
    fun getActivityEvidenceByActivityId(id: Long): EvidenceDTO {
        val activity = activityRepository.findById(id)?.toDomain() ?: throw ActivityNotFoundException(id)

        if (activity.hasEvidences) {
            return activityEvidenceService.getActivityEvidence(id, toDate(activity.insertDate)!!)
        } else {
            throw NoEvidenceInActivityException(id)
        }
    }
}
