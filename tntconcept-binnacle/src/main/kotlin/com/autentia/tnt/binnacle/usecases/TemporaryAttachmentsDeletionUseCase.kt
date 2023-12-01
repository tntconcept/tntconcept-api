package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.core.domain.AttachmentInfo
import com.autentia.tnt.binnacle.core.services.AttachmentService
import com.autentia.tnt.binnacle.repositories.AttachmentInfoRepository
import com.autentia.tnt.binnacle.services.DateService
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
class TemporaryAttachmentsDeletionUseCase internal constructor(
    private val attachmentInfoRepository: AttachmentInfoRepository,
    private val attachmentService: AttachmentService,
    private val dateService: DateService,
    private val appProperties: AppProperties,
) {

    @Transactional
    fun delete() {
        if (appProperties.binnacle.temporaryAttachments.enabled) {
            val temporaryAttachments = attachmentInfoRepository.findByIsTemporaryTrue()

            val temporaryAttachmentsLessThanOneDay =
                temporaryAttachments.filter { isMoreThanOneDay(it.toDomain()) }

            attachmentService.removeAttachments(temporaryAttachmentsLessThanOneDay)
        }
    }

    fun isMoreThanOneDay(temporaryAttachment: AttachmentInfo) =
        dateService.getDateNow().minusDays(1).isAfter(temporaryAttachment.uploadDate)

}