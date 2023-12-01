package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.AttachmentInfo
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository
import java.util.*

@Repository
internal interface AttachmentInfoDao : CrudRepository<AttachmentInfo, UUID> {
    fun findByIdAndUserId(id: UUID, userId: Long): Optional<AttachmentInfo>
    fun findByIdIn(id: List<UUID>): List<AttachmentInfo>
    fun findByIdInAndUserId(ids: List<UUID>, userId: Long): List<AttachmentInfo>
    fun existsByIdIn(id: List<UUID>): Boolean
    fun existsByIdInAndUserId(id: List<UUID>, userId: Long): Boolean
    fun deleteByIdIn(attachmentsIds: List<UUID>)
    fun findByIsTemporaryTrue(): List<AttachmentInfo>
}