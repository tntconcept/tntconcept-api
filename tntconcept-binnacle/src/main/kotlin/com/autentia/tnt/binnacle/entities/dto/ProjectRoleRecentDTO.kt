package com.autentia.tnt.binnacle.entities.dto

import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class ProjectRoleRecentDTO(
    val id: Long,
    val name: String,
    val projectName: String,
    val organizationName: String,
    val projectBillable: Boolean,
    val projectOpen: Boolean,

    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss'Z'")
    val date: LocalDateTime,
    val requireEvidence: RequireEvidence
)
