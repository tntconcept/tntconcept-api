package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.AttachmentInfo
import java.util.*

internal interface AttachmentInfoRepository {
    fun findById(id: UUID): Optional<AttachmentInfo>
    fun findByIds(ids: List<UUID>): List<AttachmentInfo>
    fun findByIsTemporaryTrue(): List<AttachmentInfo>
    fun existsAllByIds(evidencesIds: List<UUID>): Boolean
    fun save(attachmentInfo: AttachmentInfo)
    fun update(attachmentInfos: List<AttachmentInfo>)
    fun delete(attachmentsIds: List<UUID>)
}