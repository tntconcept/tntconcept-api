package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.core.domain.Attachment
import com.autentia.tnt.binnacle.core.services.AttachmentService
import com.autentia.tnt.binnacle.entities.dto.AttachmentCreationRequestDTO
import com.autentia.tnt.binnacle.entities.dto.AttachmentCreationResponseDTO
import com.autentia.tnt.security.application.checkAuthentication
import com.autentia.tnt.security.application.id
import io.micronaut.security.utils.DefaultSecurityService
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
class AttachmentCreationUseCase internal constructor(
        private val securityService: DefaultSecurityService,
        private val attachmentService: AttachmentService
) {

    @Transactional
    fun createAttachment(createAttachmentDto: AttachmentCreationRequestDTO): AttachmentCreationResponseDTO {
        val authentication = securityService.checkAuthentication()
        val userId = authentication.id()

        val attachment = this.attachmentService.createAttachment(
                fileName = createAttachmentDto.fileName,
                mimeType = createAttachmentDto.mimeType,
                file = createAttachmentDto.file,
                userId = userId)

        return this.convertToDto(attachment)
    }

    private fun convertToDto(attachment: Attachment): AttachmentCreationResponseDTO = with(attachment) {
        AttachmentCreationResponseDTO(
                id = info.id.value,
                fileName = info.fileName,
                mimeType = info.mimeType,
                uploadDate = info.uploadDate,
                isTemporary = info.isTemporary
        )
    }

}