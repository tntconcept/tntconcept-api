package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.AttachmentInfo
import com.autentia.tnt.binnacle.entities.dto.AttachmentInfoDTO
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDateTime

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AttachmentInfoConverterTest {

    private val attachmentInfoConverter = AttachmentInfoConverter()

    @Test
    fun `given domain Attachment should return DTO Attachment with converted values`() {
        val result = attachmentInfoConverter.toAttachmentInfoDTO(ATTACHMENT_INFO)

        assertEquals(ATTACHMENT_INFO_DTO, result)
    }

    @Test
    fun `given DTO Attachment should return domain Attachment with converted values`() {
        val result = attachmentInfoConverter.toAttachment(ATTACHMENT_INFO_DTO)

        assertEquals(ATTACHMENT_INFO, result)
    }

    companion object {
        private val UUID = java.util.UUID.randomUUID()
        private val ATTACHMENT_INFO_DTO = AttachmentInfoDTO(
            UUID,
            userId = 1L,
            fileName = "File.jpeg",
            mimeType = "image/jpeg",
            uploadDate = LocalDateTime.now().withSecond(0).withNano(0),
            isTemporary = true,
            path = "/"
        )

        private val ATTACHMENT_INFO = AttachmentInfo(
            UUID,
            userId = 1L,
            fileName = "File.jpeg",
            mimeType = "image/jpeg",
            uploadDate = LocalDateTime.now().withSecond(0).withNano(0),
            isTemporary = true,
            path = "/"
        )
    }
}