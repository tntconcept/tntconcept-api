package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.repositories.AttachmentFileRepository
import com.autentia.tnt.binnacle.repositories.AttachmentInfoRepository
import jakarta.inject.Singleton
import java.time.LocalDateTime
import javax.transaction.Transactional

@Singleton
class TemporaryAttachmentsDeletionUseCase internal constructor(
    private val attachmentInfoRepository: AttachmentInfoRepository,
    private val attachmentFileRepository: AttachmentFileRepository,
) {

    @Transactional
    fun delete() {
        val temporaryAttachments = attachmentInfoRepository.findByIsTemporaryTrue()

        val temporaryAttachmentsLessThanOneDay =
            temporaryAttachments.filter { isMoreThanOneDay(it) }.map { it.toDomain() }

        attachmentFileRepository.deleteActivityEvidence(temporaryAttachmentsLessThanOneDay)

        attachmentInfoRepository.deleteTemporaryList(temporaryAttachmentsLessThanOneDay.map { it.id!! })
    }

    fun isMoreThanOneDay(temporaryAttachment: com.autentia.tnt.binnacle.entities.AttachmentInfo) =
        LocalDateTime.now().minusDays(1).isAfter(temporaryAttachment.uploadDate)

}