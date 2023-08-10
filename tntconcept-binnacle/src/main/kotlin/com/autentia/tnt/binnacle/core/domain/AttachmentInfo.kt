package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.entities.AttachmentType
import java.time.LocalDateTime
import java.util.*

data class AttachmentInfo(
    val id: UUID? = null,
    val fileName: String,
    val mimeType: String,
    val uploadDate: LocalDateTime,
    val isTemporary: Boolean,
    val userId: Long,
    val type: AttachmentType,
    val path: String,

    )

