package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.core.domain.Attachment
import com.autentia.tnt.binnacle.exception.AttachmentNotFoundException
import com.autentia.tnt.binnacle.repositories.AttachmentFileRepository
import com.autentia.tnt.binnacle.repositories.AttachmentInfoRepository
import com.autentia.tnt.security.application.checkAuthentication
import io.micronaut.security.utils.SecurityService
import jakarta.inject.Singleton
import java.util.*

@Singleton
class AttachmentRetrievalUseCase internal constructor(
        private val securityService: SecurityService,
        private val attachmentInfoRepository: AttachmentInfoRepository,
        private val attachmentFileRepository: AttachmentFileRepository
) {

    fun getAttachment(id: UUID): Attachment {
        securityService.checkAuthentication()
        val attachmentInfo = attachmentInfoRepository.findById(id).orElseThrow { AttachmentNotFoundException() }
        val attachmentFile = attachmentFileRepository.getAttachmentContent(attachmentInfo)
        return Attachment(attachmentInfo, attachmentFile)
    }
}