package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.AttachmentInfo
import com.autentia.tnt.security.application.canAccessAllAttachments
import com.autentia.tnt.security.application.checkAuthentication
import com.autentia.tnt.security.application.id
import com.autentia.tnt.security.application.isAdmin
import io.micronaut.security.utils.SecurityService
import jakarta.inject.Singleton
import java.util.*

@Singleton
internal class AttachmentInfoRepositorySecured (
    private val securityService: SecurityService,
    private val attachmentInfoDao: AttachmentInfoDao,
) : AttachmentInfoRepository {

    override fun findById(id: UUID): AttachmentInfo? {
        val authentication = securityService.checkAuthentication()
        return if(authentication.canAccessAllAttachments()) {
            attachmentInfoDao.findById(id).orElse(null)
        } else {
            attachmentInfoDao.findByIdAndUserId(id, authentication.id())
        }
    }

    override fun save(attachmentInfo: AttachmentInfo): AttachmentInfo {
        return attachmentInfoDao.save(attachmentInfo)
    }
}