package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.config.createAttachmentInfoWithFilenameAndMimetype
import com.autentia.tnt.binnacle.entities.AttachmentType
import com.autentia.tnt.binnacle.exception.AttachmentNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class
AttachmentFileRepositoryIT {
    private val appProperties = AppProperties().apply {
        files.evidencesPath = "src/test/resources/attachments_test/evidences"
    }
    private val attachmentFileRepository: AttachmentFileRepository = AttachmentFileRepository(appProperties)

    @Test
    fun `should return the stored attachment`() {
        val result =
            attachmentFileRepository.getAttachment(ATTACHMENT_PATH, AttachmentType.EVIDENCE, ATTACHMENT_FILENAME)

        assertTrue(IMAGE_BYTEARRAY.contentEquals(result))
    }

    @Test
    fun `throws AttachmentNotFoundExceptionWhenAttachmentDoesNotExist`() {
        assertThrows<AttachmentNotFoundException> {
            attachmentFileRepository.getAttachment(ATTACHMENT_PATH_ERROR, AttachmentType.EVIDENCE, ATTACHMENT_FILENAME)
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "application/pdf",
            "image/png",
            "image/jpg",
            "image/jpeg",
            "image/gif"
        ]
    )
    fun `should create a new file with the respective mimetype`(mimeType: String) {

        val extension = mimeType.split("/")[1]

        val attachmentInfo = createAttachmentInfoWithFilenameAndMimetype(
            filename = "Evidence.$extension",
            mimeType = mimeType
        ).toDomain()

        attachmentFileRepository.storeAttachment(attachmentInfo, IMAGE_BYTEARRAY)

        val expectedEvidenceFilename = "${appProperties.files.evidencesPath}/${attachmentInfo.fileName}"
        val file = File(expectedEvidenceFilename)
        val content = File(expectedEvidenceFilename).readBytes()

        file.delete()
        assertThat(IMAGE_BYTEARRAY.contentEquals(content))
        assertThat(file.exists()).isFalse()
    }

    companion object {

        const val ATTACHMENT_PATH = "/"
        const val ATTACHMENT_PATH_ERROR = "/error"
        const val ATTACHMENT_FILENAME = "7a5a56cf-03c3-42fb-8c1a-91b4cbf6b42b.jpeg"

        val IMAGE_BYTEARRAY =
            File("src/test/resources/attachments_test/evidences/7a5a56cf-03c3-42fb-8c1a-91b4cbf6b42b.jpeg").readBytes()
    }
}