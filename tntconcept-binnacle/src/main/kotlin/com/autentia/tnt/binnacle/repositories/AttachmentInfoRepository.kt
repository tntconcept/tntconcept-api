package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.core.domain.AttachmentInfo
import java.util.*

internal interface AttachmentInfoRepository {
    fun findById(id: UUID): Optional<AttachmentInfo>
    fun save(attachmentInfo: AttachmentInfo)
    fun isPresent(id: UUID): Boolean
    fun findByIsTemporaryTrue(): List<AttachmentInfo>
    fun delete(attachmentsIds: List<UUID>)
}