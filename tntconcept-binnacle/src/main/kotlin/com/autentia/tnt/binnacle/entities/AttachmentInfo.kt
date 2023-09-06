package com.autentia.tnt.binnacle.entities

import org.hibernate.annotations.Type
import java.time.LocalDateTime
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "Attachment")
data class AttachmentInfo(
    @Id
    @Type(type = "uuid-char")
    var id: UUID,

    val userId: Long,
    val path: String,
    val fileName: String,
    val mimeType: String,
    var uploadDate: LocalDateTime,
    var isTemporary: Boolean,

    ) {

    fun toDomain() =
        com.autentia.tnt.binnacle.core.domain.AttachmentInfo(
            id,
            fileName,
            mimeType,
            uploadDate,
            isTemporary,
            userId,
            path,
        )

    companion object {
        fun of(attachmentInfo: com.autentia.tnt.binnacle.core.domain.AttachmentInfo) = AttachmentInfo(
                attachmentInfo.id,
                attachmentInfo.userId,
                attachmentInfo.path,
                attachmentInfo.fileName,
                attachmentInfo.mimeType,
                attachmentInfo.uploadDate,
                attachmentInfo.isTemporary
        )
    }
}
