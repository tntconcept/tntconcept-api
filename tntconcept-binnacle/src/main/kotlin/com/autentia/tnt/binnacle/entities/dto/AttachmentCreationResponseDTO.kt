package com.autentia.tnt.binnacle.entities.dto

import java.time.LocalDateTime
import java.util.*

data class AttachmentCreationResponseDTO(
    val id: UUID,
    val fileName: String,
    val mimeType: String,
    val uploadDate: LocalDateTime,
    val isTemporary: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AttachmentCreationResponseDTO

        if (id != other.id) return false
        if (fileName != other.fileName) return false
        if (mimeType != other.mimeType) return false
        if (uploadDate != other.uploadDate) return false
        if (isTemporary != other.isTemporary) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + uploadDate.hashCode()
        result = 31 * result + isTemporary.hashCode()
        return result
    }
}
