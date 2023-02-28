package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.converters.ProjectResponseConverter
import com.autentia.tnt.binnacle.entities.dto.OrganizationResponseDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectResponseDTO
import com.autentia.tnt.binnacle.usecases.ImputableOrganizationsUseCase
import com.autentia.tnt.binnacle.usecases.ImputableProjectsByOrganizationIdUseCase
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.swagger.v3.oas.annotations.Operation

@Controller("/api/organizations")
internal class OrganizationController(
    private val imputableOrganizationsUseCase: ImputableOrganizationsUseCase,
    private val imputableProjectsByOrganizationIdUseCase: ImputableProjectsByOrganizationIdUseCase,
    private val projectResponseConverter: ProjectResponseConverter
) {

    @Operation(summary = "Retrieves a list of all organizations")
    @Get
    fun getAllOrganizations(): List<OrganizationResponseDTO> =
        imputableOrganizationsUseCase.get()

    @Operation(summary = "Retrieves a list of imputable projects from an organization ID")
    @Get("/{id}/projects")
    fun getOrganizationsProjects(id: Long): List<ProjectResponseDTO> {
        val projectList = imputableProjectsByOrganizationIdUseCase.get(id)
        return projectList.map { projectResponseConverter.toProjectResponseDTO(it) }
    }

}
