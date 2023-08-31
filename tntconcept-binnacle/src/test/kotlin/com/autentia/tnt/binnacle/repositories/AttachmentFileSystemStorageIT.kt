package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.core.domain.Attachment
import com.autentia.tnt.binnacle.core.domain.AttachmentInfo
import com.autentia.tnt.binnacle.core.services.AttachmentFileSystemStorage
import com.autentia.tnt.binnacle.exception.AttachmentNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.time.LocalDateTime
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class AttachmentFileSystemStorageIT {

    private val appProperties = AppProperties().apply {
        files.attachmentsPath = "src/test/resources/attachments_test/evidences"
    }

    private val sut = AttachmentFileSystemStorage(appProperties)

    @Test
    fun `should return the stored attachment content`() {
        // Given
        val existingAttachment = `an existing attachment`()

        // When
        val result = sut.retrieveAttachmentFile(existingAttachment.info.path)

        // Then
        assertThat(result.contentEquals(existingAttachment.file)).isTrue()
    }

    @Test
    fun `throws AttachmentNotFoundExceptionWhenAttachmentDoesNotExist`() {
        val attachmentInfo = AttachmentInfo(
                id = UUID.randomUUID(),
                userId = 1L,
                path = "/non.jpg",
                fileName = "non.jpf",
                mimeType = "image/jpg",
                uploadDate = LocalDateTime.now().withSecond(0).withNano(0),
                isTemporary = true
        )

        assertThrows<AttachmentNotFoundException> {
            sut.retrieveAttachmentFile(attachmentInfo.path)
        }
    }

    @Test
    fun `should create a new attachment and store it with uuid name and extension`() {
        // Given
        val id = UUID.randomUUID()
        val attachmentInfo = AttachmentInfo(
                id = id,
                userId = 1L,
                path = "/2022/2/$id.jpeg",
                fileName = "Evidence.jpeg",
                mimeType = "image/jpeg",
                uploadDate = LocalDateTime.now().withSecond(0).withNano(0),
                isTemporary = true
        )

        val attachment = Attachment(attachmentInfo, IMAGE_BYTEARRAY)

        // When
        sut.storeAttachmentFile(attachment.info.path, attachment.file)

        // Then
        val expectedEvidenceFilename = "${appProperties.files.attachmentsPath}/${attachmentInfo.path}"
        val file = File(expectedEvidenceFilename)
        assertThat(file).exists()

        val content = File(expectedEvidenceFilename).readBytes()
        assertThat(IMAGE_BYTEARRAY.contentEquals(content))
        file.delete()
        assertThat(file).doesNotExist()
    }

    @Test
    fun `should delete an attachment`() {
        // Given
        val id = UUID.randomUUID()
        val attachmentInfo = AttachmentInfo(
                id = id,
                userId = 1L,
                path = "/2022/2/$id.jpeg",
                fileName = "Evidence.jpeg",
                mimeType = "image/jpeg",
                uploadDate = LocalDateTime.now().withSecond(0).withNano(0),
                isTemporary = true
        )

        val attachment = Attachment(attachmentInfo, IMAGE_BYTEARRAY)

        // When
        sut.storeAttachmentFile(attachment.info.path, attachment.file)
        sut.deleteAttachmentFile(attachment.info.path)

         // Then
        val attachmentFilename = "${appProperties.files.attachmentsPath}/${attachmentInfo.path}"
        val file = File(attachmentFilename)
        assertThat(file).doesNotExist()
    }

    @Test
    fun `should not delete a directory`() {
        val attachmentFilename = appProperties.files.attachmentsPath

        sut.deleteAttachmentFile(attachmentFilename)

        val file = File(attachmentFilename)
        assertThat(file).exists()
    }

    private fun `an existing attachment`(): Attachment =
            Attachment(
                    info = AttachmentInfo(
                            id = UUID.fromString("7a5a56cf-03c3-42fb-8c1a-91b4cbf6b42b"),
                            fileName = "some_image.jpeg",
                            mimeType = "image/jpeg",
                            uploadDate = LocalDateTime.now(),
                            isTemporary = false,
                            userId = 1L,
                            path = "/7a5a56cf-03c3-42fb-8c1a-91b4cbf6b42b.jpeg"
                    ),
                    file = IMAGE_BYTEARRAY
            )

    companion object {
        val IMAGE_BYTEARRAY =
                File("src/test/resources/attachments_test/evidences/7a5a56cf-03c3-42fb-8c1a-91b4cbf6b42b.jpeg").readBytes()
    }
}