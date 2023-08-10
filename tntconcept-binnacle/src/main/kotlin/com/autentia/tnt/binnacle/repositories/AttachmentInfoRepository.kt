package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.AttachmentInfo
import java.util.*

internal interface AttachmentInfoRepository {
    fun findById(id: UUID): Optional<AttachmentInfo>
    fun save(attachmentInfo: AttachmentInfo): AttachmentInfo
    fun isPresent(id: UUID): Boolean
}