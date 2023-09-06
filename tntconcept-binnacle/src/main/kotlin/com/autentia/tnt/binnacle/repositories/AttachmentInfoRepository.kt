package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.core.domain.AttachmentInfo
import com.autentia.tnt.binnacle.core.domain.AttachmentInfoId
import java.util.*

internal interface AttachmentInfoRepository {
    fun findById(id: AttachmentInfoId): Optional<AttachmentInfo>
    fun findByIds(ids: List<AttachmentInfoId>): List<AttachmentInfo>
    fun findByIsTemporaryTrue(): List<AttachmentInfo>
    fun existsAllByIds(evidencesIds: List<AttachmentInfoId>): Boolean
    fun save(attachmentInfo: AttachmentInfo)
    fun update(attachmentInfos: List<AttachmentInfo>)
    fun delete(attachmentsIds: List<AttachmentInfoId>)
}