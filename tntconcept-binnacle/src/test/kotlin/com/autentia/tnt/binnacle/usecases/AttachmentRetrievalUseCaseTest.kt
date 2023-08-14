package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createAttachmentInfo
import com.autentia.tnt.binnacle.converters.AttachmentInfoConverter
import com.autentia.tnt.binnacle.exception.AttachmentNotFoundException
import com.autentia.tnt.binnacle.repositories.AttachmentFileRepository
import com.autentia.tnt.binnacle.repositories.AttachmentInfoRepository
import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.utils.SecurityService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.mockito.kotlin.whenever
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AttachmentRetrievalUseCaseTest {

    private val attachmentRepository = mock<AttachmentInfoRepository>()
    private val attachmentFileRepository = mock<AttachmentFileRepository>()
    private val attachmentInfoConverter = AttachmentInfoConverter()
    private val securityService = mock<SecurityService>()
    private val attachmentRetrievalUseCase = AttachmentRetrievalUseCase(
        securityService,
        attachmentRepository,
        attachmentFileRepository,
        attachmentInfoConverter
    )

    @BeforeEach
    fun setUp() {
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationUser))
    }

    @Test
    fun `retrieve attachment by uuid`() {
        whenever(attachmentRepository.findById(ATTACHMENT_UUID))
            .thenReturn(Optional.of(ATTACHMENT_INFO_ENTITY))
        whenever(
            attachmentFileRepository.getAttachment(
                ATTACHMENT_INFO_ENTITY.path,
                ATTACHMENT_INFO_ENTITY.fileName
            )
        )
            .thenReturn(IMAGE_RAW)

        val attachment = attachmentRetrievalUseCase.getAttachment(ATTACHMENT_UUID)
        assertEquals(ATTACHMENT_INFO_DTO, attachment.info)
        assertTrue(Arrays.equals(IMAGE_RAW, attachment.file))
    }

    @Test
    fun `throws AttachmentNotFoundException when get an attachment by id that doesnt exists`() {
        whenever(attachmentRepository.findById(ATTACHMENT_UUID))
            .thenReturn(Optional.empty())

        assertThrows<AttachmentNotFoundException> {
            attachmentRetrievalUseCase.getAttachment(ATTACHMENT_UUID)
        }
    }

    companion object {
        private val ATTACHMENT_INFO_ENTITY = createAttachmentInfo()
        private val ATTACHMENT_INFO = ATTACHMENT_INFO_ENTITY.toDomain()
        private val ATTACHMENT_INFO_CONVERTER = AttachmentInfoConverter()
        private val ATTACHMENT_INFO_DTO = ATTACHMENT_INFO_CONVERTER.toAttachmentInfoDTO(ATTACHMENT_INFO)

        private const val IMAGE_BASE64 =
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVQYV2NgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII="
        private val ATTACHMENT_UUID = UUID.randomUUID()
        private val IMAGE_RAW = Base64.getDecoder().decode(IMAGE_BASE64)

        private const val userId = 1L
        private const val adminUserId = 3L
        private val authenticationAdmin =
            ClientAuthentication(adminUserId.toString(), mapOf("roles" to listOf("admin")))
        private val authenticationUser = ClientAuthentication(userId.toString(), mapOf("roles" to listOf("user")))

    }
}