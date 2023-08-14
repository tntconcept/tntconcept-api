package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.config.createAttachmentInfoEntityWithFilenameAndMimetype
import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.utils.SecurityService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.*

internal class AttachmentInfoRepositorySecuredTest {
    private val internalAttachmentInfoRepository = mock<InternalAttachmentInfoRepository>()
    private val securityService = mock<SecurityService>()
    private val attachmentInfoRepositorySecured =
        AttachmentInfoRepositorySecured(securityService, internalAttachmentInfoRepository)

    @Test
    fun `call findById when user is admin`() {
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationAdmin))
        attachmentInfoRepositorySecured.findById(attachmentId)

        verify(internalAttachmentInfoRepository).findById(attachmentId)
    }

    @Test
    fun `call findByIdAndUserId when user is not admin`() {
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationUser))
        attachmentInfoRepositorySecured.findById(attachmentId)

        verify(internalAttachmentInfoRepository).findByIdAndUserId(attachmentId, userId)
    }

    @Test
    fun `call save when user logged is the same as the attachment userId`() {
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationAdmin))
        attachmentInfoRepositorySecured.save(SUPPORTED_ATTACHMENT_INFO.copy(userId = 3L))

        verify(internalAttachmentInfoRepository).save(SUPPORTED_ATTACHMENT_INFO.copy(userId = 3L))
    }

    @Test
    fun `call save when user logged is not the same as the attachment userId`() {
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationUser))
        assertThrows<IllegalArgumentException> {
            attachmentInfoRepositorySecured.save(
                SUPPORTED_ATTACHMENT_INFO.copy(
                    userId = 2L
                )
            )
        }

        verifyNoInteractions(internalAttachmentInfoRepository)
    }

    @Test
    fun `call isPresent when user is admin`() {
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationAdmin))
        attachmentInfoRepositorySecured.isPresent(attachmentId)

        verify(internalAttachmentInfoRepository).findById(attachmentId)
    }

    @Test
    fun `call isPresent when user is not admin`() {
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationUser))
        attachmentInfoRepositorySecured.isPresent(attachmentId)

        verify(internalAttachmentInfoRepository).findByIdAndUserId(attachmentId, userId)
    }

    @Test
    fun `call updateIsTemporary without check authentication`() {
        val state = true
        attachmentInfoRepositorySecured.updateIsTemporary(attachmentId, state)

        verify(internalAttachmentInfoRepository).updateIsTemporary(attachmentId, state)
    }

    @Test
    fun `call findByIsTemporaryTrue without check authentication`() {
        attachmentInfoRepositorySecured.findByIsTemporaryTrue()

        verify(internalAttachmentInfoRepository).findByIsTemporaryTrue()
    }

    @Test
    fun `call deleteTemporaryList without check authentication`() {
        attachmentInfoRepositorySecured.deleteTemporaryList(listOf(SUPPORTED_ATTACHMENT_INFO.id!!))

        verify(internalAttachmentInfoRepository).deleteTemporaryList(listOf(SUPPORTED_ATTACHMENT_INFO.id!!))
    }


    companion object {
        private val attachmentId = UUID.randomUUID()
        private const val userId = 1L
        private const val adminUserId = 3L
        private val authenticationAdmin =
            ClientAuthentication(adminUserId.toString(), mapOf("roles" to listOf("admin")))
        private val authenticationUser = ClientAuthentication(userId.toString(), mapOf("roles" to listOf("user")))

        private const val IMAGE_SUPPORTED_FILENAME = "Evidence001.png"
        private const val IMAGE_SUPPORTED_MIMETYPE = "image/png"
        private val SUPPORTED_ATTACHMENT_INFO = createAttachmentInfoEntityWithFilenameAndMimetype(
            IMAGE_SUPPORTED_FILENAME,
            IMAGE_SUPPORTED_MIMETYPE
        )
    }


}