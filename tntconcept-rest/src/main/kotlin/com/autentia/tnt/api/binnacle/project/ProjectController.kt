package com.autentia.tnt.api.binnacle.project

import com.autentia.tnt.api.binnacle.ErrorResponse
import com.autentia.tnt.binnacle.entities.dto.ProjectFilterDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectResponseDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleUserDTO
import com.autentia.tnt.binnacle.exception.InvalidBlockDateException
import com.autentia.tnt.binnacle.exception.ProjectClosedException
import com.autentia.tnt.binnacle.usecases.*
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.swagger.v3.oas.annotations.Operation

@Controller("/api/project")
internal class ProjectController(
    private val projectByIdUseCase: ProjectByIdUseCase,
    private val projectRoleByProjectIdUseCase: ProjectRoleByProjectIdUseCase,
    private val blockProjectByIdUseCase: BlockProjectByIdUseCase,
    private val unblockProjectByIdUseCase: UnblockProjectByIdUseCase,
    private val projectByFilterUseCase: ProjectByFilterUseCase
) {
    @Operation(summary = "Gets projects with specified filters")
    @Get("{?projectFilterDTO*}")
    fun get(projectFilterDTO: ProjectFilterDTO): List<ProjectResponseDTO> =
        projectByFilterUseCase.getProjects(projectFilterDTO)

    @Operation(summary = "Retrieves a project´s information from its ID")
    @Get("/{id}")
    fun getProjectById(id: Long): ProjectResponseDTO {
        return projectByIdUseCase.get(id)
    }

    @Operation(summary = "Retrieves a list of project roles from a project ID")
    @Get("/{projectId}/role")
    fun getProjectRolesByProjectId(projectId: Long, @QueryValue year: Int?): List<ProjectRoleUserDTO> {
        return projectRoleByProjectIdUseCase.get(projectId, year)
    }

    @Operation(summary = "Blocks a project until given date")
    @Post("/{projectId}/block")
    fun blockProjectById(projectId: Long, @Body blockProjectRequest: BlockProjectRequestDTO): ProjectResponseDTO =
        blockProjectByIdUseCase.blockProject(projectId, blockProjectRequest.blockDate)

    @Operation(summary = "Unblocks a project")
    @Post("/{projectId}/unblock")
    fun unblockProjectById(projectId: Long): ProjectResponseDTO = unblockProjectByIdUseCase.unblockProject(projectId)

    @Error
    internal fun onProjectClosedException(request: HttpRequest<*>, e: ProjectClosedException) =
            HttpResponse.badRequest(ErrorResponse("CLOSED_PROJECT", e.message))

    @Error
    internal fun onInvalidBlockDateException(request: HttpRequest<*>, e: InvalidBlockDateException) =
            HttpResponse.badRequest(ErrorResponse("INVALID_BLOCK_DATE", e.message))

}