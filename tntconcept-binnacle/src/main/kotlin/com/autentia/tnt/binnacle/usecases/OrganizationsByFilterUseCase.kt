package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.OrganizationResponseConverter
import com.autentia.tnt.binnacle.core.domain.OrganizationType
import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.dto.OrganizationFilterDTO
import com.autentia.tnt.binnacle.entities.dto.OrganizationResponseDTO
import com.autentia.tnt.binnacle.repositories.OrganizationRepository
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
class OrganizationsByFilterUseCase internal constructor(
    private val organizationRepository: OrganizationRepository,
    private val organizationResponseConverter: OrganizationResponseConverter
){
    @Transactional
    @ReadOnly
    fun get(organizationFilter: OrganizationFilterDTO): List<OrganizationResponseDTO> {
        val organizations = organizationRepository.findAll()

        val filteredOrganizations = applyFilters(organizationFilter, organizations)

        return filteredOrganizations.map { organizationResponseConverter.toOrganizationResponseDTO(it) }
    }

    private fun applyFilters(filter: OrganizationFilterDTO, organizations: Iterable<Organization>): List<Organization> {
        var filteredOrganizations = organizations

        filter.types.forEach {
            filteredOrganizations = filteredOrganizations.filter { organization -> isOfType(organization, it) }
        }

        if(filter.imputable !== null){
            filteredOrganizations = filteredOrganizations.filter { organization ->
                organization.projects.any { project -> project.open && project.projectRoles.isNotEmpty() }
            }
        }

        return filteredOrganizations.toList()
    }

    private fun isOfType(organization: Organization, type: OrganizationType): Boolean {
        return when(type) {
            OrganizationType.CLIENT -> organization.isClient()
            OrganizationType.PROVIDER -> organization.isProvider()
            OrganizationType.PROSPECT -> organization.isProspect()
        }
    }
}