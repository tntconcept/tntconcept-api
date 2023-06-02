package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.entities.dto.ProjectResponseDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleUserDTO
import com.autentia.tnt.binnacle.usecases.ProjectByIdUseCase
import com.autentia.tnt.binnacle.usecases.ProjectRoleByProjectIdUseCase
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.swagger.v3.oas.annotations.Operation

@Controller("/api/project")
internal class ProjectController(
    private val projectByIdUseCase: ProjectByIdUseCase,
    private val projectRoleByProjectIdUseCase: ProjectRoleByProjectIdUseCase
) {
    @Operation(summary = "Retrieves a project´s information from its ID")
    @Get("/{id}")
    fun getProjectById(id: Long): ProjectResponseDTO {
        return projectByIdUseCase.get(id)
    }

    @Operation(summary = "Retrieves a list of project roles from a project ID")
    @Get("/{projectId}/role")
    fun getProjectRolesByProjectId(projectId: Long, @QueryValue year: Int): List<ProjectRoleUserDTO> {
        return projectRoleByProjectIdUseCase.get(projectId, year)
    }
}