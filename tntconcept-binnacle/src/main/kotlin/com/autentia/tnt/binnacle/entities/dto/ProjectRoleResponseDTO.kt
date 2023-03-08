package com.autentia.tnt.binnacle.entities.dto

import com.autentia.tnt.binnacle.entities.RequireEvidence

data class ProjectRoleResponseDTO (
    val id: Long,
    val name: String,
    val requireEvidence: RequireEvidence
)
