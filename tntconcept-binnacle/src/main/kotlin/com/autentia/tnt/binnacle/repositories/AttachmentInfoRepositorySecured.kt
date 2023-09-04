package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.core.domain.AttachmentInfo
import com.autentia.tnt.binnacle.core.domain.AttachmentInfoId
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

    override fun findById(id: AttachmentInfoId): Optional<AttachmentInfo> {
        val authentication = securityService.checkAuthentication()
        return if (authentication.canAccessAllAttachments())
            attachmentInfoDao.findById(id.value).map { Mapper.toDomain(it) }
        else
            attachmentInfoDao.findByIdAndUserId(id.value, authentication.id()).map { Mapper.toDomain(it) }
    }

    override fun findByIds(ids: List<AttachmentInfoId>): List<AttachmentInfo> {
        val authentication = securityService.checkAuthentication()
        return if (authentication.canAccessAllAttachments())
            attachmentInfoDao.findAllById(ids.map { it.value }).map { Mapper.toDomain(it) }
        else
            attachmentInfoDao.findAllByIdAndUserId(ids.map { it.value }, authentication.id()).map { Mapper.toDomain(it) }
    }

    override fun existsAllByIds(evidencesIds: List<AttachmentInfoId>): Boolean {
        val authentication = securityService.checkAuthentication()
        return if (authentication.canAccessAllAttachments())
            attachmentInfoDao.existsAllById(evidencesIds.map { it.value })
        else
            attachmentInfoDao.existsAllByIdAndUserId(evidencesIds.map { it.value }, authentication.id())
    }

    override fun save(attachmentInfo: AttachmentInfo) {
        val authentication = securityService.checkAuthentication()
        require(attachmentInfo.userId == authentication.id()) { "User cannot upload attachment" }

        attachmentInfoDao.save(Mapper.toJpaEntity(attachmentInfo))
    }

    override fun save(attachmentInfos: List<AttachmentInfo>) {
        attachmentInfos.forEach { save(it) }
    }

    override fun findByIsTemporaryTrue(): List<AttachmentInfo> =
            attachmentInfoDao.findByIsTemporaryTrue().map { Mapper.toDomain(it) }

    override fun delete(attachmentsIds: List<AttachmentInfoId>) =
            attachmentInfoDao.delete(attachmentsIds.map { it.value })

    object Mapper {
        fun toJpaEntity(attachmentInfo: AttachmentInfo): com.autentia.tnt.binnacle.entities.AttachmentInfo = with(attachmentInfo) {
            com.autentia.tnt.binnacle.entities.AttachmentInfo(
                    id = id.value,
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
                    id = AttachmentInfoId(id),
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