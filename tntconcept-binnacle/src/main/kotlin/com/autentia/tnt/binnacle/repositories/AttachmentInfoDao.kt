package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.AttachmentInfo
import io.micronaut.data.annotation.Query
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository
import java.util.*

@Repository
internal interface AttachmentInfoDao : CrudRepository<AttachmentInfo, UUID> {
    fun findByIdAndUserId(id: UUID, userId: Long): Optional<AttachmentInfo>

    fun findByIsTemporaryTrue(): List<AttachmentInfo>

    @Query("UPDATE AttachmentInfo SET isTemporary = :state WHERE id = :id")
    fun updateIsTemporary(id: UUID, state: Boolean)

    @Query("DELETE FROM AttachmentInfo WHERE id IN (:attachmentsIds)")
    fun delete(attachmentsIds: List<UUID>)

}