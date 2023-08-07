package com.autentia.tnt.binnacle.core.domain

import java.time.LocalDateTime
import java.util.*

data class AttachmentInfo(
    val id: UUID,
    val type: AttachmentType,
    val path: String,
    val fileName: String,
    val mimeType: String,
    val uploadDate: LocalDateTime,
    val isTemporary: Boolean,
)

enum class AttachmentType {
    EVIDENCE
}
