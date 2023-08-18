package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.config.createAttachmentInfoEntityWithFilenameAndMimetype
import com.autentia.tnt.binnacle.core.domain.Attachment
import com.autentia.tnt.binnacle.core.domain.AttachmentInfo
import com.autentia.tnt.binnacle.exception.AttachmentNotFoundException
import com.autentia.tnt.binnacle.usecases.AttachmentRetrievalUseCaseTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import java.time.LocalDateTime
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class AttachmentFileRepositoryIT {

    private val appProperties = AppProperties().apply {
        files.attachmentsPath = "src/test/resources/attachments_test/evidences"
    }

    private val attachmentFileRepository: AttachmentFileRepository = AttachmentFileRepository(appProperties)

    @Test
    fun `should return the stored attachment content`() {
        // Given
        val existingAttachment = `an existing attachment`()

        // When
        val result = attachmentFileRepository.getAttachmentContent(existingAttachment.info)

        // Then
        assertThat(result.contentEquals(existingAttachment.file)).isTrue()
    }

    @Test
    fun `throws AttachmentNotFoundExceptionWhenAttachmentDoesNotExist`() {
        val attachmentInfo = createAttachmentInfoEntityWithFilenameAndMimetype(
                filename = "non.jpeg",
                mimeType = "image/jpeg"
        ).toDomain()

        assertThrows<AttachmentNotFoundException> {
            attachmentFileRepository.getAttachmentContent(attachmentInfo)
        }
    }

    @Test
    fun `should create a new attachment and store it with uuid name and extension`() {
        // Given
        val attachmentInfo = AttachmentInfo(
                id = UUID.randomUUID(),
                userId = 1L,
                path = "/",
                fileName = "Evidence.jpeg",
                mimeType = "image/jpeg",
                uploadDate = LocalDateTime.now().withSecond(0).withNano(0),
                isTemporary = true
        )

        val attachment = Attachment(attachmentInfo, IMAGE_BYTEARRAY)

        // When
        attachmentFileRepository.storeAttachment(attachment)

        // Then
        val expectedEvidenceFilename = "${appProperties.files.attachmentsPath}/${attachmentInfo.id}.jpeg"
        val file = File(expectedEvidenceFilename)
        assertThat(file).exists()

        val content = File(expectedEvidenceFilename).readBytes()
        assertThat(IMAGE_BYTEARRAY.contentEquals(content))
        file.delete()
        assertThat(file).doesNotExist()
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
                            path = "/"
                    ),
                    file = IMAGE_BYTEARRAY
            )

    companion object {
        val IMAGE_BYTEARRAY =
                File("src/test/resources/attachments_test/evidences/7a5a56cf-03c3-42fb-8c1a-91b4cbf6b42b.jpeg").readBytes()
    }
}