package com.autentia.tnt.binnacle.entities

import org.hibernate.annotations.Type
import java.time.LocalDateTime
import java.util.*
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "Attachment")
data class AttachmentInfo(

    @Id
    //@Type("uuid-char")
    var id: UUID,

    val userId: Long,
    val path: String,
    val fileName: String,
    val mimeType: String,
    val uploadDate: LocalDateTime,
    val isTemporary: Boolean,

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
        fun of(attachment: com.autentia.tnt.binnacle.core.domain.Attachment) = AttachmentInfo(
            attachment.info.id,
                attachment.info.userId,
                attachment.info.path,
                attachment.info.fileName,
                attachment.info.mimeType,
                attachment.info.uploadDate,
                attachment.info.isTemporary
        )
    }
}
