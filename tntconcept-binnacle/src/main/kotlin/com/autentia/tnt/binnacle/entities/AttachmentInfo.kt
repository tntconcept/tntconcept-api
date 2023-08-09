package com.autentia.tnt.binnacle.entities

import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Type
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name="Attachment")
data class AttachmentInfo(

    @Id
    @GeneratedValue (generator = "UUID")
    @GenericGenerator (name = "UUID",strategy = "org.hibernate.id.UUIDGenerator")
    @Type(type = "uuid-char")
    val id: UUID? = null,

    val userId: Long,
    @Enumerated(EnumType.STRING)
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
            userId,
            type,
            path,
            fileName,
            mimeType,
            uploadDate,
            isTemporary
        )
}

enum class AttachmentType {
    EVIDENCE
}
