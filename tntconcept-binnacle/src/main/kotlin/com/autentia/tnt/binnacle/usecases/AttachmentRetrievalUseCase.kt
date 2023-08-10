package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.AttachmentInfoConverter
import com.autentia.tnt.binnacle.entities.dto.AttachmentDTO
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
    private val attachmentFileRepository: AttachmentFileRepository,
    private val attachmentInfoConverter: AttachmentInfoConverter,
) {

    fun getAttachment(id: UUID): AttachmentDTO {
        securityService.checkAuthentication()

        val attachmentInfo = attachmentInfoRepository.findById(id).orElseThrow { AttachmentNotFoundException() }

        val attachmentFile =
            attachmentFileRepository.getAttachment(attachmentInfo.path, attachmentInfo.type, attachmentInfo.fileName)
        return AttachmentDTO(attachmentInfoConverter.toAttachmentInfoDTO(attachmentInfo.toDomain()), attachmentFile)
    }
}