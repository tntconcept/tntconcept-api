package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.converters.AttachmentInfoConverter
import com.autentia.tnt.binnacle.entities.AttachmentType
import com.autentia.tnt.binnacle.entities.dto.AttachmentInfoDTO
import com.autentia.tnt.binnacle.exception.AttachmentMimeTypeNotSupportedException
import com.autentia.tnt.binnacle.repositories.AttachmentFileRepository
import com.autentia.tnt.binnacle.repositories.AttachmentInfoRepository
import com.autentia.tnt.security.application.checkAuthentication
import com.autentia.tnt.security.application.id
import io.micronaut.security.utils.DefaultSecurityService
import jakarta.inject.Singleton
import java.time.LocalDateTime

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

    fun storeAttachment(attachmentFile: ByteArray, filename: String, mimeType: String): AttachmentInfoDTO {
        val authentication = securityService.checkAuthentication()
        val userId = authentication.id()

        if (!isMimeTypeSupported(mimeType)) throw AttachmentMimeTypeNotSupportedException("Mime type $mimeType is not supported")

        val attachmentInfoToCreateDTO = constructNewAttachmentInfo(filename, mimeType, userId)


        val attachmentInfoToCreate = attachmentInfoConverter.toAttachment(attachmentInfoToCreateDTO);
        attachmentFileRepository.storeAttachment(attachmentInfoToCreate, attachmentFile)

        val attachmentInfoEntityToCreate = com.autentia.tnt.binnacle.entities.AttachmentInfo.of(attachmentInfoToCreate)
        val savedAttachment = attachmentInfoRepository.save(attachmentInfoEntityToCreate)

        return attachmentInfoConverter.toAttachmentInfoDTO(savedAttachment.toDomain())
    }

    private fun constructNewAttachmentInfo(filename: String, mimeType: String, userId: Long): AttachmentInfoDTO =
        AttachmentInfoDTO(
            null,
            userId,
            AttachmentType.EVIDENCE,
            "/",
            filename,
            mimeType,
            LocalDateTime.now().withSecond(0).withNano(0),
            true
        )

    private fun isMimeTypeSupported(mimeType: String): Boolean = supportedMimeExtensions.containsKey(mimeType)
}