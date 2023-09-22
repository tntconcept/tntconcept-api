package com.autentia.tnt.api.binnacle.project

import com.autentia.tnt.binnacle.entities.dto.ProjectFilterDTO
import io.micronaut.core.annotation.Introspected

@Introspected
data class ProjectFilterRequest(
    val organizationId: Long,
    val organizationIds: String?,
    val open: Boolean? = null,
) {
    fun toDto(): ProjectFilterDTO = ProjectFilterDTO(
        organizationId,
        getOrganizationIds(organizationIds),
        open,
    )

    private fun getOrganizationIds(organizationIds: String?): List<Long> {
        return if(!organizationIds.isNullOrEmpty()) {
            organizationIds.split(",").map { it.toLong() }
        } else {
            listOf()
        }
    }
}