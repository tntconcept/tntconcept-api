package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.config.createAttachmentInfoEntityWithFilenameAndMimetype
import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.utils.SecurityService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.*

@TestInstance(PER_CLASS)
internal class AttachmentInfoRepositorySecuredTest {
    private val securityService = mock<SecurityService>()
    private val attachmentInfoDao = mock<AttachmentInfoDao>()

    private val attachmentInfoRepositorySecured =
            AttachmentInfoRepositorySecured(securityService, attachmentInfoDao)

    @BeforeEach
    fun setUp() {
        reset(attachmentInfoDao)
    }

    @Test
    fun `call findById when user is admin`() {
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationAdmin))
        attachmentInfoRepositorySecured.findById(attachmentId)

        verify(attachmentInfoDao).findById(attachmentId)
    }

    @Test
    fun `call findByIdAndUserId when user is not admin`() {
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationUser))
        attachmentInfoRepositorySecured.findById(attachmentId)

        verify(attachmentInfoDao).findByIdAndUserId(attachmentId, userId)
    }

    @Test
    fun `call save when user logged is the same as the attachment userId`() {
        whenever(securityService.authentication).thenReturn(Optional.of(authenticationAdmin))
        attachmentInfoRepositorySecured.save(SUPPORTED_ATTACHMENT_INFO.copy(userId = 3L))

        verify(attachmentInfoDao).save(SUPPORTED_ATTACHMENT_INFO.copy(id = SUPPORTED_ATTACHMENT_INFO.id, userId = 3L))
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

        verifyNoInteractions(attachmentInfoDao)
    }

    @Test
    fun `call findByIsTemporaryTrue without check authentication`() {
        attachmentInfoRepositorySecured.findByIsTemporaryTrue()

        verify(attachmentInfoDao).findByIsTemporaryTrue()
    }

    @Test
    fun `call deleteTemporaryList without check authentication`() {
        attachmentInfoRepositorySecured.delete(listOf(SUPPORTED_ATTACHMENT_INFO.id))

        verify(attachmentInfoDao).deleteByIdIn(listOf(SUPPORTED_ATTACHMENT_INFO.id))
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

        private val SUPPORTED_ATTACHMENT_INFO= createAttachmentInfoEntityWithFilenameAndMimetype(
                IMAGE_SUPPORTED_FILENAME,
                IMAGE_SUPPORTED_MIMETYPE
        )
    }


}