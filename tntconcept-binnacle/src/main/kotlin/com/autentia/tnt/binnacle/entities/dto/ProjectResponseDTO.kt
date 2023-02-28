package com.autentia.tnt.binnacle.entities.dto

data class ProjectResponseDTO(
    val id: Long,
    val name: String,
    val open: Boolean,
    val billable: Boolean
)
