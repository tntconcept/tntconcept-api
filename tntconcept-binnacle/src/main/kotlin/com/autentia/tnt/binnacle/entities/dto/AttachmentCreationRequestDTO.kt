package com.autentia.tnt.binnacle.entities.dto

import java.time.LocalDateTime

data class AttachmentCreationRequestDTO(
        val fileName: String,
        val mimeType: String,
        val uploadDate: LocalDateTime,
        val file: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AttachmentCreationRequestDTO

        if (fileName != other.fileName) return false
        if (mimeType != other.mimeType) return false
        if (uploadDate != other.uploadDate) return false
        if (!file.contentEquals(other.file)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + uploadDate.hashCode()
        result = 31 * result + file.contentHashCode()
        return result
    }
}