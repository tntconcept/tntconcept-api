package com.autentia.tnt.api.binnacle.organization

import com.autentia.tnt.binnacle.entities.dto.OrganizationResponseDTO

data class OrganizationResponse(
        val id: Long,
        val name: String
) {
    companion object {
        fun from(organizationResponseDTO: OrganizationResponseDTO) =
            OrganizationResponse(
                organizationResponseDTO.id,
                organizationResponseDTO.name
            )
    }
}
