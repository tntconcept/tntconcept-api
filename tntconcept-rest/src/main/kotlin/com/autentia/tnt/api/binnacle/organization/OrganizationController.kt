package com.autentia.tnt.api.binnacle.organization

import com.autentia.tnt.api.binnacle.project.ProjectResponse
import com.autentia.tnt.binnacle.core.domain.OrganizationType
import com.autentia.tnt.binnacle.usecases.ImputableOrganizationsUseCase
import com.autentia.tnt.binnacle.usecases.ImputableProjectsByOrganizationIdUseCase
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.swagger.v3.oas.annotations.Operation

@Controller("/api/organizations")
internal class OrganizationController(
    private val imputableOrganizationsUseCase: ImputableOrganizationsUseCase,
    private val imputableProjectsByOrganizationIdUseCase: ImputableProjectsByOrganizationIdUseCase,
) {

    @Operation(summary = "Retrieves a list of all organizations")
    @Get
    fun getAllOrganizations(@QueryValue type: OrganizationTypeFilter?): List<OrganizationResponse> =
        imputableOrganizationsUseCase.get(getOrganizationType(type)).map { OrganizationResponse.from(it) }

    @Operation(summary = "Retrieves a list of imputable projects from an organization ID")
    @Get("/{id}/projects")
    fun getOrganizationsProjects(id: Long): List<ProjectResponse> =
        imputableProjectsByOrganizationIdUseCase.get(id).map { ProjectResponse.from(it) }

    private fun getOrganizationType(type: OrganizationTypeFilter?): OrganizationType? {
        return if (type !== null) {
            OrganizationType.valueOf(type.name)
        } else {
            return null
        }
    }

}
