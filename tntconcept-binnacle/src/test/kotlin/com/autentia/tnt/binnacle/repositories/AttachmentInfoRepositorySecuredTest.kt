package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.config.createAttachmentInfo
import com.autentia.tnt.binnacle.converters.AttachmentInfoConverter
import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.utils.SecurityService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.*

internal class AttachmentInfoRepositorySecuredTest {
    private val attachmentInfoDao = mock<AttachmentInfoDao> ()
    private val securityService = mock<SecurityService>()
    private val attachmentInfoRepositorySecured = AttachmentInfoRepositorySecured(securityService, attachmentInfoDao)

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



    companion object {
        private val attachmentId = UUID.randomUUID()
        private const val userId = 1L
        private const val adminUserId = 3L
        private val authenticationAdmin =
            ClientAuthentication(adminUserId.toString(), mapOf("roles" to listOf("admin")))
        private val authenticationUser = ClientAuthentication(userId.toString(), mapOf("roles" to listOf("user")))

    }


}