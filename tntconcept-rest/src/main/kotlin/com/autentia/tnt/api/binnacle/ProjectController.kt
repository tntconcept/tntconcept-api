package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.entities.dto.ProjectResponseDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleDTO
import com.autentia.tnt.binnacle.usecases.ProjectByIdUseCase
import com.autentia.tnt.binnacle.usecases.ProjectRolesByProjectIdUseCase
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.swagger.v3.oas.annotations.Operation

@Controller("/api/projects")
internal class ProjectController(
    private val projectByIdUseCase: ProjectByIdUseCase,
    private val projectRolesByProjectIdUseCase: ProjectRolesByProjectIdUseCase,
) {

    @Operation(summary = "Retrieves a project´s information from its ID")
    @Get("/{id}")
    fun getProjectById(id: Long): ProjectResponseDTO {
        return projectByIdUseCase.get(id)
    }

    @Operation(summary = "Retrieves a list of project roles from a project ID")
    @Get("/{id}/roles")
    fun getProjectRolesByProjectId(id: Int): List<ProjectRoleDTO> {
        return projectRolesByProjectIdUseCase.get(id)
    }
}
