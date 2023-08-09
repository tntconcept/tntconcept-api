package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.AttachmentInfo
import com.autentia.tnt.binnacle.entities.dto.AttachmentInfoDTO
import jakarta.inject.Singleton

@Singleton
class AttachmentInfoConverter {
    fun toAttachmentInfoDTO(attachmentInfo: AttachmentInfo) =
        AttachmentInfoDTO( attachmentInfo.id!!,attachmentInfo.userId, attachmentInfo.type, attachmentInfo.path,
            attachmentInfo.fileName, attachmentInfo.mimeType, attachmentInfo.uploadDate, attachmentInfo.isTemporary )
}