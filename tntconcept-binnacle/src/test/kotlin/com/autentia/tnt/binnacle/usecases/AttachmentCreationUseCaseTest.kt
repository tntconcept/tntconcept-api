package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.core.domain.Attachment
import com.autentia.tnt.binnacle.core.domain.AttachmentInfo
import com.autentia.tnt.binnacle.entities.dto.AttachmentCreationRequestDTO
import com.autentia.tnt.binnacle.exception.AttachmentMimeTypeNotSupportedException
import com.autentia.tnt.binnacle.repositories.AttachmentFileRepository
import com.autentia.tnt.binnacle.repositories.AttachmentInfoRepository
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
    private val attachmentFileRepository = mock<AttachmentFileRepository>()
    private val attachmentInfoRepository = mock<AttachmentInfoRepository>()

    private val appProperties = AppProperties().apply {
        files.attachmentsPath = "src/test/resources/attachments_test/evidences"
        files.supportedMimeTypes = mapOf(
                Pair("image/jpeg", "jpg"),
        )
    }
    private val sut = AttachmentCreationUseCase(
            securityService,
            attachmentFileRepository,
            attachmentInfoRepository,
            appProperties
    )

    @BeforeEach
    fun setUp() {
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationUser))
        reset(attachmentInfoRepository, attachmentFileRepository)
    }

    @Test
    fun `create and persist an attachment via repository and store the file`() {
        // Given
        val attachmentCreationRequestDTO = `request with a jpeg as attachment`()
        doNothing().whenever(this.attachmentFileRepository).storeAttachment(any())
        doNothing().whenever(this.attachmentInfoRepository).save(any())

        // When
        val result = this.sut.createAttachment(attachmentCreationRequestDTO)

        // Then
        assertThat(result.fileName).isEqualTo(attachmentCreationRequestDTO.fileName)
        assertThat(result.mimeType).isEqualTo(attachmentCreationRequestDTO.mimeType)
        assertThat(result.uploadDate).isEqualTo(attachmentCreationRequestDTO.uploadDate)
        assertThat(result.isTemporary).isEqualTo(true)

        // Verify
        val attachmentCaptor = argumentCaptor<Attachment>()
        val attachmentInfoCaptor = argumentCaptor<AttachmentInfo>()

        verify(attachmentFileRepository).storeAttachment(attachmentCaptor.capture())
        verify(attachmentInfoRepository).save(attachmentInfoCaptor.capture())

        assertThat(attachmentCaptor.firstValue.file.contentEquals(attachmentCreationRequestDTO.file)).isTrue()

        assertThat(attachmentInfoCaptor.firstValue.fileName).isEqualTo(attachmentCreationRequestDTO.fileName)
        assertThat(attachmentInfoCaptor.firstValue.mimeType).isEqualTo(attachmentCreationRequestDTO.mimeType)
        assertThat(attachmentInfoCaptor.firstValue.uploadDate).isEqualTo(attachmentCreationRequestDTO.uploadDate)
        assertThat(attachmentInfoCaptor.firstValue.isTemporary).isEqualTo(true)
        assertThat(attachmentInfoCaptor.firstValue.userId).isEqualTo(USER_ID)
        assertThat(attachmentInfoCaptor.firstValue.path).isEqualTo(DEFAULT_PATH)


        verifyNoMoreInteractions(attachmentFileRepository, attachmentInfoRepository)
    }

    @Test
    fun `throws AttachmentMimeTypeNotSupportedException when mimetype is not valid`() {
        // Given
        val attachment = `a request with a not supported attachment`()

        // When, Then
        assertThrows<AttachmentMimeTypeNotSupportedException> {
            sut.createAttachment(attachment)
        }

        // Verify
        verifyNoInteractions(attachmentFileRepository, attachmentInfoRepository)
    }

    private fun `request with a jpeg as attachment`() = AttachmentCreationRequestDTO(
            fileName = "some_image.jpeg",
            mimeType = "image/jpeg",
            uploadDate = LocalDateTime.now(),
            file = IMAGE_BYTEARRAY
    )

    private fun `a request with a not supported attachment`() =
            AttachmentCreationRequestDTO(
                    fileName = "some_json.json",
                    mimeType = "application/json",
                    uploadDate = LocalDateTime.now(),
                    file = SOME_BYTE_ARRAY
            )

    companion object {
        private const val DEFAULT_PATH = "/"

        private val IMAGE_BYTEARRAY =
                File("src/test/resources/attachments_test/evidences/7a5a56cf-03c3-42fb-8c1a-91b4cbf6b42b.jpeg").readBytes()


        private val SOME_BYTE_ARRAY = ByteArray(2)

        private const val USER_ID: Long = 1L
        private val authenticationUser =
                ClientAuthentication(USER_ID.toString(), mapOf("roles" to listOf("user")))
    }
}