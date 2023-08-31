package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.core.domain.Attachment
import com.autentia.tnt.binnacle.core.domain.AttachmentInfo
import com.autentia.tnt.binnacle.core.services.AttachmentService
import com.autentia.tnt.binnacle.exception.AttachmentNotFoundException
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
    private val securityService = mock<SecurityService>()
    private val attachmentService = mock<AttachmentService>()

    private val sut = AttachmentRetrievalUseCase(
            securityService, attachmentService
    )

    @BeforeEach
    fun setUp() {
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationUser))
        reset(attachmentService)
    }

    @Test
    fun `retrieve an existing attachment by uuid`() {
        // Given
        val existingAttachment = `an existing attachment`()
        val existingUUID = existingAttachment.info.id
        doReturn(existingAttachment).whenever(attachmentService).findAttachment(existingUUID)

        // When
        val result = this.sut.getAttachment(existingUUID)

        // Then
        assertThat(result).isEqualTo(existingAttachment)

        // Verify
        verify(attachmentService).findAttachment(existingUUID)
    }

    @Test
    fun `throws AttachmentNotFoundException when get an attachment by id that doesnt exists`() {
        // Given
        val nonExistingUUID = UUID.randomUUID()
        whenever(attachmentService.findAttachment(nonExistingUUID)).thenThrow(AttachmentNotFoundException())

        // When, Then
        assertThrows<AttachmentNotFoundException> {
            sut.getAttachment(nonExistingUUID)
        }

        // Verify
        verify(attachmentService).findAttachment(nonExistingUUID)
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