package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.config.createAttachmentInfoDtoWithFileNameAndMimeType
import com.autentia.tnt.binnacle.config.createAttachmentInfoWithFilenameAndMimetype
import com.autentia.tnt.binnacle.converters.AttachmentInfoConverter
import com.autentia.tnt.binnacle.entities.dto.AttachmentDTO
import com.autentia.tnt.binnacle.exception.AttachmentMimeTypeNotSupportedException
import com.autentia.tnt.binnacle.repositories.AttachmentFileRepository
import com.autentia.tnt.binnacle.repositories.AttachmentInfoRepository
import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.utils.DefaultSecurityService
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.mockito.Mockito.*
import org.mockito.kotlin.whenever
import java.io.File
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AttachmentCreationUseCaseTest {
    private val securityService = mock<DefaultSecurityService>()
    private val attachmentFileRepository = mock<AttachmentFileRepository>()
    private val attachmentInfoRepository = mock<AttachmentInfoRepository>()
    private val attachmentInfoConverter = AttachmentInfoConverter()
    private val appProperties = AppProperties().apply {
        files.evidencesPath = "src/test/resources/attachments_test/evidences"
        files.supportedMimeTypes = mapOf(
            Pair("image/png", "png"),
        )
    }
    private val attachmentCreationUseCase =
        AttachmentCreationUseCase(
            securityService,
            attachmentFileRepository,
            attachmentInfoRepository,
            attachmentInfoConverter,
            appProperties
        )

    @BeforeEach
    fun setUp() {
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationUser))
    }

    @Test
    fun `store attachment info when attachment is valid`() {

        whenever(attachmentInfoRepository.save(SUPPORTED_ATTACHMENT_INFO_ENTITY.copy(id = null))).thenReturn(
            SUPPORTED_ATTACHMENT_INFO_ENTITY
        )

        val savedAttachment = attachmentCreationUseCase.storeAttachment(
            SUPPORTED_ATTACHMENT_DTO
        )

        assertNotNull(savedAttachment.id)
        assertTrue(savedAttachment.isTemporary)
        assertEquals(SUPPORTED_ATTACHMENT_INFO_ENTITY.fileName, savedAttachment.fileName)
    }

    @Test
    fun `throws AttachmentMimeTypeNotSupportedException when mimetype is not valid`() {
        assertThrows<AttachmentMimeTypeNotSupportedException> {
            attachmentCreationUseCase.storeAttachment(
                UNSUPPORTED_ATTACHMENT_DTO
            )
        }
    }

    companion object {
        private val IMAGE_BYTEARRAY =
            File("src/test/resources/attachments_test/evidences/7a5a56cf-03c3-42fb-8c1a-91b4cbf6b42b.jpeg").readBytes()

        private const val IMAGE_SUPPORTED_FILENAME = "Evidence001.png"
        private const val IMAGE_UNSUPPORTED_FILENAME = "Evidence001.json"
        private const val IMAGE_SUPPORTED_MIMETYPE = "image/png"
        private const val IMAGE_UNSUPPORTED_MIMETYPE = "application/json"

        private val SUPPORTED_ATTACHMENT_INFO_ENTITY =
            createAttachmentInfoWithFilenameAndMimetype(
                filename = IMAGE_SUPPORTED_FILENAME,
                mimeType = IMAGE_SUPPORTED_MIMETYPE
            )

        private val SUPPORTED_ATTACHMENT_INFO_DTO = createAttachmentInfoDtoWithFileNameAndMimeType(
            IMAGE_SUPPORTED_FILENAME,
            IMAGE_SUPPORTED_MIMETYPE
        )
        private val UNSUPPORTED_ATTACHMENT_INFO_DTO = createAttachmentInfoDtoWithFileNameAndMimeType(
            IMAGE_UNSUPPORTED_FILENAME,
            IMAGE_UNSUPPORTED_MIMETYPE
        )

        private val SUPPORTED_ATTACHMENT_DTO = AttachmentDTO(SUPPORTED_ATTACHMENT_INFO_DTO, IMAGE_BYTEARRAY)
        private val UNSUPPORTED_ATTACHMENT_DTO = AttachmentDTO(UNSUPPORTED_ATTACHMENT_INFO_DTO, IMAGE_BYTEARRAY)

        private const val userId = 1L
        private val authenticationUser =
            ClientAuthentication(userId.toString(), mapOf("roles" to listOf("user")))
    }
}