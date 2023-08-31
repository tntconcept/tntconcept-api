package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.core.domain.AttachmentInfo
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
            attachmentInfoDao.findById(id).map { Mapper.toDomain(it) }
        else
            attachmentInfoDao.findByIdAndUserId(id, authentication.id()).map { Mapper.toDomain(it) }
    }

    override fun save(attachmentInfo: AttachmentInfo) {
        val authentication = securityService.checkAuthentication()
        require(attachmentInfo.userId == authentication.id()) { "User cannot upload attachment" }

        attachmentInfoDao.save(Mapper.toJpaEntity(attachmentInfo))
    }

    override fun isPresent(id: UUID): Boolean {
        val authentication = securityService.checkAuthentication()

        return if (authentication.canAccessAllAttachments())
            attachmentInfoDao.findById(id).isPresent
        else
            attachmentInfoDao.findByIdAndUserId(id, authentication.id()).isPresent
    }

    override fun findByIsTemporaryTrue(): List<AttachmentInfo> =
        attachmentInfoDao.findByIsTemporaryTrue().map { Mapper.toDomain(it) }

    override fun delete(attachmentsIds: List<UUID>) =
        attachmentInfoDao.delete(attachmentsIds)

    object Mapper {
        fun toJpaEntity(attachmentInfo: AttachmentInfo): com.autentia.tnt.binnacle.entities.AttachmentInfo = with(attachmentInfo) {
            com.autentia.tnt.binnacle.entities.AttachmentInfo(
                    id = id,
                    userId = userId,
                    path = path,
                    fileName = fileName,
                    mimeType = mimeType,
                    uploadDate = uploadDate,
                    isTemporary = isTemporary
            )
        }

        fun toDomain(attachmentInfo: com.autentia.tnt.binnacle.entities.AttachmentInfo): AttachmentInfo = with(attachmentInfo) {
            AttachmentInfo(
                    id = id,
                    userId = userId,
                    path = path,
                    fileName = fileName,
                    mimeType = mimeType,
                    uploadDate = uploadDate,
                    isTemporary = isTemporary
            )
        }

    }

}