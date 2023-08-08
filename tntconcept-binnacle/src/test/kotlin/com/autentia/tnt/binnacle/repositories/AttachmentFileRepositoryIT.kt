package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.core.domain.AttachmentType
import com.autentia.tnt.binnacle.exception.AttachmentNotFoundException

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class AttachmentFileRepositoryIT {
    private val appProperties = AppProperties().apply {
        files.evidencesPath = "src/test/resources/attachments_test/evidences"
        files.supportedMimeTypes = mapOf(
            Pair("application/pdf", "pdf"),
            Pair("image/jpg", "jpg"),
            Pair("image/jpeg", "jpeg"),
            Pair("image/png", "png"),
            Pair("image/gif", "gif")
        )
    }
    val attachmentFileRepository: AttachmentFileRepository = AttachmentFileRepository(appProperties)

    @Test
    fun `should return the stored attachment`() {
        val result = attachmentFileRepository.getAttachment(ATTACHMENT_PATH, AttachmentType.EVIDENCE,ATTACHMENT_FILENAME)
        assertTrue(IMAGE_BYTEARRAY.contentEquals(result))
    }

    @Test
    fun `throws AttachmentNotFoundExceptionWhenAttachmentDoesNotExist` () {
        assertThrows<AttachmentNotFoundException> {
            attachmentFileRepository.getAttachment(ATTACHMENT_PATH_ERROR, AttachmentType.EVIDENCE,ATTACHMENT_FILENAME)
        }
    }

    companion object {
        const val ATTACHMENT_PATH = "/"
        const val ATTACHMENT_PATH_ERROR = "/error"
        const val ATTACHMENT_FILENAME = "7a5a56cf-03c3-42fb-8c1a-91b4cbf6b42b.jpeg"
        val IMAGE_BYTEARRAY =  File("src/test/resources/attachments_test/evidences/7a5a56cf-03c3-42fb-8c1a-91b4cbf6b42b.jpeg").readBytes()
    }
}