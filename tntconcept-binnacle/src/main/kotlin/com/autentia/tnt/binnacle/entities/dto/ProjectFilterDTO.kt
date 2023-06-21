package com.autentia.tnt.binnacle.entities.dto

import io.micronaut.core.annotation.Introspected

@Introspected
data class ProjectFilterDTO(
    val organizationId: Long,
    val open: Boolean? = null,
)
