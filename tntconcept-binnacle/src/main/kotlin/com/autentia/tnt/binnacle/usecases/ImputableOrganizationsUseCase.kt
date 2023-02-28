package com.autentia.tnt.binnacle.usecases


import com.autentia.tnt.binnacle.converters.OrganizationResponseConverter
import com.autentia.tnt.binnacle.entities.dto.OrganizationResponseDTO
import com.autentia.tnt.binnacle.repositories.OrganizationRepository
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
class ImputableOrganizationsUseCase internal constructor(
    private val organizationRepository: OrganizationRepository,
    private val organizationResponseConverter: OrganizationResponseConverter

) {

    @Transactional
    @ReadOnly
    fun get(): List<OrganizationResponseDTO> {
        val organizations = organizationRepository.findAll()

        return organizations
            .filter { organization ->
                organization.projects.any { project -> project.open && project.projectRoles.isNotEmpty() }
            }
            .map { organizationResponseConverter.toOrganizationResponseDTO(it) }
    }

}
