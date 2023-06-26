package com.autentia.tnt.api.binnacle.project

import com.autentia.tnt.binnacle.entities.dto.ProjectFilterDTO
import io.micronaut.core.annotation.Introspected

@Introspected
data class ProjectFilterRequest(
    val organizationId: Long,
    val open: Boolean? = null,
) {
    fun toDto(): ProjectFilterDTO = ProjectFilterDTO(
        organizationId,
        open,
    )
}