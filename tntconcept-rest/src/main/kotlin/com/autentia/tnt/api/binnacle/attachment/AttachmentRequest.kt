package com.autentia.tnt.api.binnacle.attachment

import com.autentia.tnt.binnacle.entities.dto.AttachmentDTO
import io.micronaut.http.multipart.CompletedFileUpload

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


    fun toDto() =
        AttachmentDTO(
            attachmentInfoRequest.toDto(),
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