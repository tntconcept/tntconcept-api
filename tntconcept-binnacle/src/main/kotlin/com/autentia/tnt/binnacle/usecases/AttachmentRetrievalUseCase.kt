package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.core.domain.Attachment
import com.autentia.tnt.binnacle.core.domain.AttachmentInfoId
import com.autentia.tnt.binnacle.core.services.AttachmentService
import com.autentia.tnt.security.application.checkAuthentication
import io.micronaut.security.utils.SecurityService
import jakarta.inject.Singleton
import java.util.*

@Singleton
class AttachmentRetrievalUseCase internal constructor(
        private val securityService: SecurityService,
        private val attachmentService: AttachmentService
) {

    fun getAttachment(id: UUID): Attachment {
        securityService.checkAuthentication()
        return attachmentService.findAttachment(AttachmentInfoId(id))
    }
}