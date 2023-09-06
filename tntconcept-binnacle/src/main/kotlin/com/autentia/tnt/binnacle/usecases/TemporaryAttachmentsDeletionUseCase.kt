package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.core.domain.AttachmentInfo
import com.autentia.tnt.binnacle.core.services.AttachmentFileSystemStorage
import com.autentia.tnt.binnacle.repositories.AttachmentInfoRepository
import com.autentia.tnt.binnacle.services.DateService
import jakarta.inject.Singleton
import java.time.LocalDateTime
import javax.transaction.Transactional

@Singleton
class TemporaryAttachmentsDeletionUseCase internal constructor(
    private val attachmentInfoRepository: AttachmentInfoRepository,
    private val attachmentFileSystemStorage: AttachmentFileSystemStorage,
    private val dateService: DateService,
    private val appProperties: AppProperties,
) {

    @Transactional
    fun delete() {
        if (appProperties.binnacle.temporaryAttachments.enabled) {
            val temporaryAttachments = attachmentInfoRepository.findByIsTemporaryTrue()

            val temporaryAttachmentsLessThanOneDay =
                temporaryAttachments.filter { isMoreThanOneDay(it.toDomain()) }

            temporaryAttachmentsLessThanOneDay.forEach { attachmentFileSystemStorage.deleteAttachmentFile(it.path) }
            attachmentInfoRepository.delete(temporaryAttachmentsLessThanOneDay.map { it.id })
        }
    }

    fun isMoreThanOneDay(temporaryAttachment: AttachmentInfo) =
        dateService.getDateNow().minusDays(1).isAfter(temporaryAttachment.uploadDate)

}