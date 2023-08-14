package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.converters.AttachmentInfoConverter
import com.autentia.tnt.binnacle.entities.dto.AttachmentDTO
import com.autentia.tnt.binnacle.entities.dto.AttachmentInfoDTO
import com.autentia.tnt.binnacle.exception.AttachmentMimeTypeNotSupportedException
import com.autentia.tnt.binnacle.repositories.AttachmentFileRepository
import com.autentia.tnt.binnacle.repositories.AttachmentInfoRepository
import com.autentia.tnt.security.application.checkAuthentication
import com.autentia.tnt.security.application.id
import io.micronaut.security.utils.DefaultSecurityService
import jakarta.inject.Singleton

@Singleton
class AttachmentCreationUseCase internal constructor(
    private val securityService: DefaultSecurityService,
    private val attachmentFileRepository: AttachmentFileRepository,
    private val attachmentInfoRepository: AttachmentInfoRepository,
    private val attachmentInfoConverter: AttachmentInfoConverter,
    appProperties: AppProperties,
) {
    private val supportedMimeExtensions: Map<String, String>

    init {
        supportedMimeExtensions = appProperties.files.supportedMimeTypes
    }

    fun storeAttachment(attachmentDTO: AttachmentDTO): AttachmentInfoDTO {
        val authentication = securityService.checkAuthentication()
        val userId = authentication.id()

        val attachmentInfoDto = establishAttachmentInfoDTO(attachmentDTO, userId)

        if (!isMimeTypeSupported(attachmentInfoDto.mimeType)) throw AttachmentMimeTypeNotSupportedException("Mime type ${attachmentInfoDto.mimeType} is not supported")

        val attachmentInfoToCreate = attachmentInfoConverter.toAttachment(attachmentInfoDto);
        attachmentFileRepository.storeAttachment(attachmentInfoToCreate, attachmentDTO.file)

        val attachmentInfoEntityToCreate = com.autentia.tnt.binnacle.entities.AttachmentInfo.of(attachmentInfoToCreate)
        val savedAttachment = attachmentInfoRepository.save(attachmentInfoEntityToCreate)

        return attachmentInfoConverter.toAttachmentInfoDTO(savedAttachment.toDomain())
    }

    private fun establishAttachmentInfoDTO(
        attachmentDTO: AttachmentDTO,
        userId: Long,
    ): AttachmentInfoDTO {
        val attachmentInfoDto = attachmentDTO.info
        attachmentInfoDto.userId = userId
        attachmentInfoDto.path = "/"
        return attachmentInfoDto
    }

    private fun isMimeTypeSupported(mimeType: String): Boolean = supportedMimeExtensions.containsKey(mimeType)
}