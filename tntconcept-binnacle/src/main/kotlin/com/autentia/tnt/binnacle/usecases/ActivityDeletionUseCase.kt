package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.exception.ActivityNotFoundException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.AttachmentInfoRepository
import com.autentia.tnt.binnacle.validators.ActivityValidator
import com.autentia.tnt.security.application.canAccessAllActivities
import com.autentia.tnt.security.application.checkAuthentication
import io.micronaut.security.utils.SecurityService
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
class ActivityDeletionUseCase internal constructor(
        private val activityRepository: ActivityRepository,
        private val activityValidator: ActivityValidator,
        private val securityService: SecurityService,
        private val attachmentInfoRepository: AttachmentInfoRepository
) {

    @Transactional
    fun deleteActivityById(id: Long) {
        val activityToDelete = activityRepository.findById(id) ?: throw ActivityNotFoundException(id)
        val activityToDeleteDomain = activityToDelete.toDomain()
        val authentication = securityService.checkAuthentication()

        if (authentication.canAccessAllActivities()) {
            activityValidator.checkAllAccessActivityIsValidForDeletion(activityToDeleteDomain)
        } else {
            activityValidator.checkActivityIsValidForDeletion(activityToDeleteDomain)
        }

        if (activityToDelete.hasEvidences()) {
            val attachments = attachmentInfoRepository.findByIds(activityToDeleteDomain.evidences)
            val attachmentsUpdated = attachments.map { it.copy(isTemporary = true) }
            attachmentInfoRepository.save(attachmentsUpdated)
        }

        activityRepository.deleteById(id)
    }
}
