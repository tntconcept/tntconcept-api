package com.autentia.tnt.api.binnacle.attachment

import com.autentia.tnt.binnacle.entities.dto.AttachmentCreationRequestDTO
import io.micronaut.http.multipart.CompletedFileUpload
import java.time.LocalDateTime

data class AttachmentRequest(
    private val attachmentInfoRequest: AttachmentInfoRequest,
    private val attachmentFile: ByteArray,
) {

    companion object {
        fun of(attachmentFile: CompletedFileUpload) = AttachmentRequest(
            AttachmentInfoRequest(attachmentFile.filename, attachmentFile.contentType.get().toString()),
            attachmentFile.bytes
        )
    }

    fun toAttachmentCreationRequestDTO() = AttachmentCreationRequestDTO(
            fileName = attachmentInfoRequest.fileName,
            mimeType = attachmentInfoRequest.mimeType,
            uploadDate = LocalDateTime.now().withSecond(0).withNano(0),
            attachmentFile
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AttachmentRequest) return false

        if (attachmentInfoRequest != other.attachmentInfoRequest) return false
        return attachmentFile.contentEquals(other.attachmentFile)
    }

    override fun hashCode(): Int {
        var result = attachmentInfoRequest.hashCode()
        result = 31 * result + attachmentFile.contentHashCode()
        return result
    }
}