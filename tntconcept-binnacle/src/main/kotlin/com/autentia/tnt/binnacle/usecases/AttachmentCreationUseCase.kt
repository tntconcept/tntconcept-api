package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.core.domain.Attachment
import com.autentia.tnt.binnacle.core.domain.AttachmentInfo
import com.autentia.tnt.binnacle.entities.dto.AttachmentCreationRequestDTO
import com.autentia.tnt.binnacle.entities.dto.AttachmentCreationResponseDTO
import com.autentia.tnt.binnacle.exception.AttachmentMimeTypeNotSupportedException
import com.autentia.tnt.binnacle.repositories.AttachmentFileRepository
import com.autentia.tnt.binnacle.repositories.AttachmentInfoRepository
import com.autentia.tnt.security.application.checkAuthentication
import com.autentia.tnt.security.application.id
import io.micronaut.security.utils.DefaultSecurityService
import jakarta.inject.Singleton
import java.util.*
import javax.transaction.Transactional

@Singleton
class AttachmentCreationUseCase internal constructor(
        private val securityService: DefaultSecurityService,
        private val attachmentFileRepository: AttachmentFileRepository,
        private val attachmentInfoRepository: AttachmentInfoRepository,
        appProperties: AppProperties,
) {

    private val supportedMimeExtensions: Map<String, String> = appProperties.files.supportedMimeTypes

    @Transactional
    fun createAttachment(createAttachmentDto: AttachmentCreationRequestDTO): AttachmentCreationResponseDTO {
        val authentication = securityService.checkAuthentication()
        val userId = authentication.id()

        if (!isMimeTypeSupported(createAttachmentDto.mimeType))
            throw AttachmentMimeTypeNotSupportedException("Mime type ${createAttachmentDto.mimeType} is not supported")

        val attachment = createAttachment(createAttachmentDto, userId)

        attachmentFileRepository.storeAttachment(attachment)
        attachmentInfoRepository.save(attachment.info)

        return this.convertToDto(attachment)
    }

    private fun convertToDto(attachment: Attachment): AttachmentCreationResponseDTO = with(attachment) {
        AttachmentCreationResponseDTO(
                id = info.id,
                fileName = info.fileName,
                mimeType = info.mimeType,
                uploadDate = info.uploadDate,
                isTemporary = info.isTemporary
        )
    }

    private fun createAttachment(
            attachmentDTO: AttachmentCreationRequestDTO,
            userId: Long,
    ): Attachment {
        val attachmentInfo = AttachmentInfo(
                id = UUID.randomUUID(),
                fileName = attachmentDTO.fileName,
                mimeType = attachmentDTO.mimeType,
                uploadDate = attachmentDTO.uploadDate,
                isTemporary = true,
                userId = userId,
                path = "/"
        )

        val file = attachmentDTO.file

        return Attachment(attachmentInfo, file)
    }

    private fun isMimeTypeSupported(mimeType: String): Boolean = supportedMimeExtensions.containsKey(mimeType)
}