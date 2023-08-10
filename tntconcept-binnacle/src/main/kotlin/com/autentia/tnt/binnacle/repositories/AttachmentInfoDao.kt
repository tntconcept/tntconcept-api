package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.AttachmentInfo
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository
import java.util.*

@Repository
internal interface AttachmentInfoDao : CrudRepository<AttachmentInfo, UUID> {
    fun findByIdAndUserId(id: UUID, userId: Long): Optional<AttachmentInfo>
}