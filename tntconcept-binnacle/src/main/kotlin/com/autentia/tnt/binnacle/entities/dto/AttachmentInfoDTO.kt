package com.autentia.tnt.binnacle.entities.dto

import java.time.LocalDateTime
import java.util.*

data class AttachmentInfoDTO(
    val id: UUID? = null,
    val fileName: String,
    val mimeType: String,
    val uploadDate: LocalDateTime,
    val isTemporary: Boolean,
    var userId: Long? = null,
    var path: String? = null,
)