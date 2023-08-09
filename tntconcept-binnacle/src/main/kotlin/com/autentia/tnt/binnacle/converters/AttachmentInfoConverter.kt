package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.AttachmentInfo
import com.autentia.tnt.binnacle.entities.dto.AttachmentInfoDTO
import jakarta.inject.Singleton

@Singleton
class AttachmentInfoConverter {
    fun toAttachmentInfoDTO(attachmentInfo: AttachmentInfo) =
        AttachmentInfoDTO(
            attachmentInfo.id!!, attachmentInfo.userId, attachmentInfo.type, attachmentInfo.path,
            attachmentInfo.fileName, attachmentInfo.mimeType, attachmentInfo.uploadDate, attachmentInfo.isTemporary
        )

    fun toAttachment(attachmentInfoDTO: AttachmentInfoDTO) =
        AttachmentInfo(
            attachmentInfoDTO.id,
            attachmentInfoDTO.userId,
            attachmentInfoDTO.type,
            attachmentInfoDTO.path,
            attachmentInfoDTO.fileName,
            attachmentInfoDTO.mimeType,
            attachmentInfoDTO.uploadDate,
            attachmentInfoDTO.isTemporary
        )
}