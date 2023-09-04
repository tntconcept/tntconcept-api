package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.core.domain.Attachment
import com.autentia.tnt.binnacle.core.domain.AttachmentInfo
import com.autentia.tnt.binnacle.core.domain.AttachmentInfoId
import com.autentia.tnt.binnacle.core.services.AttachmentService
import com.autentia.tnt.binnacle.entities.dto.AttachmentCreationRequestDTO
import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.utils.DefaultSecurityService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.mockito.kotlin.*
import java.io.File
import java.time.LocalDateTime
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AttachmentCreationUseCaseTest {
    private val securityService = mock<DefaultSecurityService>()
    private val attachmentService = mock<AttachmentService>()

    private val sut = AttachmentCreationUseCase(
            securityService,
            attachmentService
    )

    @BeforeEach
    fun setUp() {
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationUser))
        reset(attachmentService)
    }

    private fun `attachment with a jpeg`(attachmentCreationRequestDTO: AttachmentCreationRequestDTO): Any {
        val id = AttachmentInfoId(UUID.randomUUID())
        return Attachment(
                info = AttachmentInfo(
                        id = id,
                        fileName = attachmentCreationRequestDTO.fileName,
                        mimeType = attachmentCreationRequestDTO.mimeType,
                        uploadDate = DATE,
                        path = "/2023/2/${id.value}.jpeg",
                        isTemporary = true,
                        userId = USER_ID
                ),
                file = attachmentCreationRequestDTO.file
        )
    }

    @Test
    fun `create and persist an attachment via repository and store the file`() {
        // Given
        val attachmentCreationRequestDTO = `request with a jpeg as attachment`()

        val attachment = `attachment with a jpeg`(attachmentCreationRequestDTO)
        doReturn(attachment).whenever(this.attachmentService).createAttachment(attachmentCreationRequestDTO.fileName,
                attachmentCreationRequestDTO.mimeType, attachmentCreationRequestDTO.file, USER_ID)

        // When
        val result = this.sut.createAttachment(attachmentCreationRequestDTO)

        // Then
        assertThat(result.fileName).isEqualTo(attachmentCreationRequestDTO.fileName)
        assertThat(result.mimeType).isEqualTo(attachmentCreationRequestDTO.mimeType)
        assertThat(result.uploadDate).isEqualTo(attachmentCreationRequestDTO.uploadDate)
        assertThat(result.isTemporary).isEqualTo(true)

        // Verify
        verify(attachmentService).createAttachment(attachmentCreationRequestDTO.fileName, attachmentCreationRequestDTO.mimeType, attachmentCreationRequestDTO.file, USER_ID)
    }

    private fun `request with a jpeg as attachment`() = AttachmentCreationRequestDTO(
            fileName = "some_image.jpeg",
            mimeType = "image/jpeg",
            uploadDate = DATE,
            file = IMAGE_BYTEARRAY
    )

    companion object {
        private val DATE = LocalDateTime.of(2023, 2, 1, 10, 0)
        private val IMAGE_BYTEARRAY =
                File("src/test/resources/attachments_test/evidences/7a5a56cf-03c3-42fb-8c1a-91b4cbf6b42b.jpeg").readBytes()

        private const val USER_ID: Long = 1L
        private val authenticationUser =
                ClientAuthentication(USER_ID.toString(), mapOf("roles" to listOf("user")))
    }
}