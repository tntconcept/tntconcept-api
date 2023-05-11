package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.dto.OrganizationResponseDTO
import jakarta.inject.Singleton

@Singleton
class OrganizationResponseConverter {

    fun toOrganizationResponseDTO(organization: Organization) =
        OrganizationResponseDTO(
            organization.id,
            organization.name
        )

    fun toOrganizationResponseDTO(organization: com.autentia.tnt.binnacle.core.domain.Organization) =
        OrganizationResponseDTO(organization.id, organization.name)
}
