package com.autentia.tnt.api.binnacle.organization

import com.autentia.tnt.binnacle.core.domain.OrganizationType
import com.autentia.tnt.binnacle.entities.dto.OrganizationFilterDTO
import io.micronaut.core.annotation.Introspected

@Introspected
data class OrganizationFilterRequest(
    val types: String?,
    val imputable: Boolean?,
) {
    fun toDto(): OrganizationFilterDTO =
        OrganizationFilterDTO(
            getOrganizationTypes(types),
            imputable,
        )

    private fun getOrganizationTypes(types: String?): List<OrganizationType> {
        return if (!types.isNullOrEmpty()) {
            val organizationTypesRequest = types.split(",")
            organizationTypesRequest.map { OrganizationType.valueOf(it) }
        } else {
            listOf()
        }
    }
}