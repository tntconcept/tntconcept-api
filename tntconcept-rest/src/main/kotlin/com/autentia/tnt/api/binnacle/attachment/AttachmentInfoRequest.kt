package com.autentia.tnt.api.binnacle.attachment

import com.autentia.tnt.binnacle.entities.dto.AttachmentInfoDTO
import java.time.LocalDateTime

data class AttachmentInfoRequest(
    private val fileName: String,
    private val mimeType: String,
) {

    fun toDto() = AttachmentInfoDTO(
        id = null,
        fileName = fileName,
        mimeType = mimeType,
        uploadDate = LocalDateTime.now().withSecond(0).withNano(0),
        isTemporary = true
    )
}