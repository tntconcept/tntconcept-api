package com.autentia.tnt.api.binnacle.project

import com.autentia.tnt.api.binnacle.ErrorResponse
import com.autentia.tnt.api.binnacle.projectrole.ProjectRoleUserResponse
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
    @Get("{?projectFilterRequest*}")
    fun get(projectFilterRequest: ProjectFilterRequest): List<ProjectResponse> =
        projectByFilterUseCase.getProjects(projectFilterRequest.toDto()).map { ProjectResponse.from(it) }

    @Operation(summary = "Retrieves a project´s information from its ID")
    @Get("/{id}")
    fun getProjectById(id: Long): ProjectResponse =
         ProjectResponse.from(projectByIdUseCase.get(id))

    @Operation(summary = "Retrieves a list of project roles from a project ID")
    @Get("/{projectId}/role")
    fun getProjectRolesByProjectId(projectId: Long, @QueryValue year: Int?): List<ProjectRoleUserResponse> =
        projectRoleByProjectIdUseCase.get(projectId, year).map { ProjectRoleUserResponse.from(it) }

    @Operation(summary = "Blocks a project until given date")
    @Post("/{projectId}/block")
    fun blockProjectById(projectId: Long, @Body blockProjectRequest: BlockProjectRequest): ProjectResponse =
        ProjectResponse.from(blockProjectByIdUseCase.blockProject(projectId, blockProjectRequest.blockDate))

    @Operation(summary = "Unblocks a project")
    @Post("/{projectId}/unblock")
    fun unblockProjectById(projectId: Long): ProjectResponse = ProjectResponse.from(unblockProjectByIdUseCase.unblockProject(projectId))

    @Error
    internal fun onProjectClosedException(request: HttpRequest<*>, e: ProjectClosedException) =
            HttpResponse.badRequest(ErrorResponse("CLOSED_PROJECT", e.message))

    @Error
    internal fun onInvalidBlockDateException(request: HttpRequest<*>, e: InvalidBlockDateException) =
            HttpResponse.badRequest(ErrorResponse("INVALID_BLOCK_DATE", e.message))

}