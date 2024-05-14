package com.autentia.tnt.binnacle.entities.dto

import java.time.YearMonth

data class SubcontractedActivityResponseDTO (
    val duration: Int,
    val description: String,
    val id: Long,
    val projectRoleId: Long,
    val month: YearMonth,
    val userId: Long
)