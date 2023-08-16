package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.AttachmentInfo
import com.autentia.tnt.security.application.canAccessAllAttachments
import com.autentia.tnt.security.application.checkAuthentication
import com.autentia.tnt.security.application.id
import io.micronaut.context.annotation.Primary
import io.micronaut.security.utils.SecurityService
import jakarta.inject.Singleton
import java.util.*

@Singleton
@Primary
internal class AttachmentInfoRepositorySecured(
    private val securityService: SecurityService,
    private val internalAttachmentRepository: InternalAttachmentRepository,
) : AttachmentInfoRepository {

    override fun findById(id: UUID): Optional<AttachmentInfo> {
        val authentication = securityService.checkAuthentication()
        return if (authentication.canAccessAllAttachments())
            internalAttachmentRepository.findById(id)
        else
            internalAttachmentRepository.findByIdAndUserId(id, authentication.id())
    }

    override fun save(attachmentInfo: AttachmentInfo): AttachmentInfo {
        val authentication = securityService.checkAuthentication()
        require(attachmentInfo.userId == authentication.id()) { "User cannot upload attachment" }

        return internalAttachmentRepository.save(attachmentInfo)
    }

    override fun isPresent(id: UUID): Boolean {
        val authentication = securityService.checkAuthentication()

        return if (authentication.canAccessAllAttachments())
            internalAttachmentRepository.findById(id).isPresent
        else
            internalAttachmentRepository.findByIdAndUserId(id, authentication.id()).isPresent
    }

    override fun updateIsTemporary(id: UUID, state: Boolean) {
        internalAttachmentRepository.updateIsTemporary(id, state)
    }
}