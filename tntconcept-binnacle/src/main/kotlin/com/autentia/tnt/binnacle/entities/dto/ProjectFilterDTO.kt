package com.autentia.tnt.binnacle.entities.dto

import io.micronaut.core.annotation.Introspected

@Introspected
data class ProjectFilterDTO(
    val organizationId: Long,
    val organizationIds: List<Long>,
    val open: Boolean? = null,
)
