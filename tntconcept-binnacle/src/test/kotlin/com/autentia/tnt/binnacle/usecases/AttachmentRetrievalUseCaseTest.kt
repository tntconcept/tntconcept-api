package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.core.domain.Attachment
import com.autentia.tnt.binnacle.core.domain.AttachmentInfo
import com.autentia.tnt.binnacle.exception.AttachmentNotFoundException
import com.autentia.tnt.binnacle.repositories.AttachmentFileRepository
import com.autentia.tnt.binnacle.repositories.AttachmentInfoRepository
import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.utils.SecurityService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import java.io.File
import java.time.LocalDateTime
import java.util.*

@TestInstance(PER_CLASS)
class AttachmentRetrievalUseCaseTest {
    private val attachmentInfoRepository = mock<AttachmentInfoRepository>()
    private val attachmentFileRepository = mock<AttachmentFileRepository>()
    private val securityService = mock<SecurityService>()

    private val sut = AttachmentRetrievalUseCase(
            securityService,
            attachmentInfoRepository,
            attachmentFileRepository
    )

    @BeforeEach
    fun setUp() {
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationUser))
        reset(attachmentInfoRepository, attachmentFileRepository)
    }

    @Test
    fun `retrieve an existing attachment by uuid`() {
        // Given
        val existingAttachment = `an existing attachment`()
        val existingUUID = existingAttachment.info.id

        whenever(attachmentInfoRepository.findById(existingUUID)).thenReturn(Optional.of(existingAttachment.info))
        whenever(attachmentFileRepository.getAttachmentContent(existingAttachment.info)).thenReturn(existingAttachment.file)

        // When
        val result = this.sut.getAttachment(existingUUID)

        // Then
        assertThat(result).isEqualTo(existingAttachment)

        verify(attachmentInfoRepository).findById(existingUUID)
        verify(attachmentFileRepository).getAttachmentContent(existingAttachment.info)
    }

    @Test
    fun `throws AttachmentNotFoundException when get an attachment by id that doesnt exists`() {
        // Given
        val nonExistingUUID = UUID.randomUUID()
        whenever(attachmentInfoRepository.findById(nonExistingUUID)).thenReturn(Optional.empty())

        // When, Then
        assertThrows<AttachmentNotFoundException> {
            sut.getAttachment(nonExistingUUID)
        }

        // Verify
        verify(attachmentInfoRepository).findById(nonExistingUUID)
        verifyNoInteractions(attachmentFileRepository)
    }

    private fun `an existing attachment`(): Attachment =
            Attachment(
                    info = AttachmentInfo(
                            id = UUID.randomUUID(),
                            fileName = "some_image.jpeg",
                            mimeType = "image/jpeg",
                            uploadDate = LocalDateTime.now(),
                            isTemporary = false,
                            userId = USER_ID,
                            path = "/"
                    ),
                    file = IMAGE_BYTEARRAY
            )


    companion object {
        private val IMAGE_BYTEARRAY =
                File("src/test/resources/attachments_test/evidences/7a5a56cf-03c3-42fb-8c1a-91b4cbf6b42b.jpeg").readBytes()

        private const val USER_ID: Long = 1L

        private val authenticationUser = ClientAuthentication(USER_ID.toString(), mapOf("roles" to listOf("user")))
    }
}