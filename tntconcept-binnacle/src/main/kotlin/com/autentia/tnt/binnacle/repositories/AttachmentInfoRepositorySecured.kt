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

    override fun findByIds(ids: List<UUID>): List<AttachmentInfo> {
        val authentication = securityService.checkAuthentication()
        return if (authentication.canAccessAllAttachments())
            attachmentInfoDao.findByIdIn(ids)
        else
            attachmentInfoDao.findByIdInAndUserId(ids, authentication.id())
    }

    override fun existsAllByIds(evidencesIds: List<UUID>): Boolean {
        val authentication = securityService.checkAuthentication()
        return if (authentication.canAccessAllAttachments())
            attachmentInfoDao.existsByIdIn(evidencesIds)
        else
            attachmentInfoDao.existsByIdInAndUserId(evidencesIds, authentication.id())
    }

    override fun save(attachmentInfo: AttachmentInfo) {
        val authentication = securityService.checkAuthentication()
        require(attachmentInfo.userId == authentication.id()) { "User cannot upload attachment" }

        attachmentInfoDao.save(attachmentInfo)
    }

    override fun update(attachmentInfos: List<AttachmentInfo>) {
        val authentication = securityService.checkAuthentication()

        attachmentInfos.forEach { attachmentInfo ->
            require(attachmentInfo.userId == authentication.id()) { "User cannot update attachment" }
            attachmentInfoDao.update(attachmentInfo)
        }
    }

    override fun findByIsTemporaryTrue(): List<AttachmentInfo> = attachmentInfoDao.findByIsTemporaryTrue()

    override fun delete(attachmentsIds: List<UUID>) = attachmentInfoDao.deleteByIdIn(attachmentsIds)


}