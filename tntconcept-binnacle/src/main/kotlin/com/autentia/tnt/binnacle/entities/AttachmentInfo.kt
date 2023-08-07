package com.autentia.tnt.binnacle.entities

import com.autentia.tnt.binnacle.core.domain.AttachmentType
import java.time.LocalDateTime
import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
data class AttachmentInfo(

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: UUID,
    val type: AttachmentType,
    val path: String,
    val fileName: String,
    val mimeType: String,
    val uploadDate: LocalDateTime,
    val isTemporary: Boolean,
) {

    fun toDomain() =
        com.autentia.tnt.binnacle.core.domain.AttachmentInfo(
            id,
            type,
            path,
            fileName,
            mimeType,
            uploadDate,
            isTemporary
        )
}