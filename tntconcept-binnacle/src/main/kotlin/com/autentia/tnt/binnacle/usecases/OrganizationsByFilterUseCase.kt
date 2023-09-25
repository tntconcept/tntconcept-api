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
        var filteredOrganizations = applyTypesFilter(filter.types, organizations)
        filteredOrganizations = applyIsImputableFilter(filter.imputable, filteredOrganizations)

        return filteredOrganizations.toList()
    }

    private fun applyIsImputableFilter(
        imputable: Boolean?,
        organizations: Iterable<Organization>
    ): Iterable<Organization> {
        var filteredOrganizations = organizations
        if (imputable !== null) {
            filteredOrganizations = if (imputable) {
                filteredOrganizations.filter { organization ->
                    organization.projects.any { project -> project.open && project.projectRoles.isNotEmpty() }
                }
            } else {
               filteredOrganizations.filter { organization ->
                    organization.projects.all { project -> !project.open || project.projectRoles.isEmpty() }
                }
            }
        }
        return filteredOrganizations
    }

    private fun applyTypesFilter(
        types: List<OrganizationType>,
        organizations: Iterable<Organization>
    ): Iterable<Organization> {
        if (types.isNotEmpty()) {
            val filteredOrganizations = mutableSetOf<Organization>()
            types.forEach {
                filteredOrganizations.addAll(organizations.filter { organization -> isOfType(organization, it) }
                    .toMutableList())
            }
            return filteredOrganizations.toList()
        }
        return organizations
    }

    private fun isOfType(organization: Organization, type: OrganizationType): Boolean {
        return when(type) {
            OrganizationType.CLIENT -> organization.isClient()
            OrganizationType.PROVIDER -> organization.isProvider()
            OrganizationType.PROSPECT -> organization.isProspect()
        }
    }
}