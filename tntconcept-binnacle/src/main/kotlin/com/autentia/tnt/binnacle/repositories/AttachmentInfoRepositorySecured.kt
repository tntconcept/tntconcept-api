package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.AttachmentInfo
import com.autentia.tnt.security.application.canAccessAllAttachments
import com.autentia.tnt.security.application.checkAuthentication
import com.autentia.tnt.security.application.id
import io.micronaut.security.utils.SecurityService
import jakarta.inject.Singleton
import java.util.*

@Singleton
internal class AttachmentInfoRepositorySecured(
    private val securityService: SecurityService,
    private val attachmentInfoDao: AttachmentInfoDao,
) : AttachmentInfoRepository {

    override fun findById(id: UUID): Optional<AttachmentInfo> {
        val authentication = securityService.checkAuthentication()
        return if (authentication.canAccessAllAttachments())
            attachmentInfoDao.findById(id)
        else
            attachmentInfoDao.findByIdAndUserId(id, authentication.id())
    }

    override fun save(attachmentInfo: AttachmentInfo): AttachmentInfo {
        val authentication = securityService.checkAuthentication()
        require(attachmentInfo.userId == authentication.id()) { "User cannot upload attachment" }

        return attachmentInfoDao.save(attachmentInfo)
    }

    override fun isPresent(id: UUID): Boolean {
        val authentication = securityService.checkAuthentication()

        return if (authentication.canAccessAllAttachments())
            attachmentInfoDao.findById(id).isPresent
        else
            attachmentInfoDao.findByIdAndUserId(id, authentication.id()).isPresent
    }
}