package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.AttachmentInfo
import jakarta.inject.Singleton
import java.util.*

@Singleton
internal class InternalAttachmentInfoRepository(
    private val attachmentInfoDao: AttachmentInfoDao,
) : AttachmentInfoRepository {
    override fun findById(id: UUID): Optional<AttachmentInfo> =
        attachmentInfoDao.findById(id)

    override fun save(attachmentInfo: AttachmentInfo): AttachmentInfo =
        attachmentInfoDao.save(attachmentInfo)

    override fun isPresent(id: UUID): Boolean =
        attachmentInfoDao.findById(id).isPresent

    override fun updateIsTemporary(id: UUID, state: Boolean) =
        attachmentInfoDao.updateIsTemporary(id, state)

    override fun findByIsTemporaryTrue(): List<AttachmentInfo> =
        attachmentInfoDao.findByIsTemporaryTrue()

    override fun deleteTemporaryList(temporaryAttachments: List<UUID>) {
        attachmentInfoDao.deleteTemporaryList(temporaryAttachments)
    }

    fun findByIdAndUserId(id: UUID, userId: Long): Optional<AttachmentInfo> =
        attachmentInfoDao.findByIdAndUserId(id, userId)

}