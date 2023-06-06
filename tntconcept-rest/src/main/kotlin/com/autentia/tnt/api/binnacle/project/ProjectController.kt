package com.autentia.tnt.api.binnacle.project

import com.autentia.tnt.api.binnacle.ErrorResponse
import com.autentia.tnt.binnacle.entities.dto.ProjectResponseDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleUserDTO
import com.autentia.tnt.binnacle.usecases.BlockProjectByIdUseCase
import com.autentia.tnt.binnacle.usecases.ProjectByIdUseCase
import com.autentia.tnt.binnacle.usecases.ProjectRoleByProjectIdUseCase
import com.autentia.tnt.binnacle.usecases.UnblockProjectByIdUseCase
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse

@Controller("/api/project")
internal class ProjectController(
    private val projectByIdUseCase: ProjectByIdUseCase,
    private val projectRoleByProjectIdUseCase: ProjectRoleByProjectIdUseCase,
    private val blockProjectByIdUseCase: BlockProjectByIdUseCase,
    private val unblockProjectByIdUseCase: UnblockProjectByIdUseCase
) {
    @Operation(summary = "Retrieves a project´s information from its ID")
    @Get("/{id}")
    fun getProjectById(id: Long): ProjectResponseDTO {
        return projectByIdUseCase.get(id)
    }

    @Operation(summary = "Retrieves a list of project roles from a project ID")
    @Get("/{projectId}/role")
    fun getProjectRolesByProjectId(projectId: Long): List<ProjectRoleUserDTO> {
        return projectRoleByProjectIdUseCase.get(projectId)
    }

    @Operation(
        summary = "Blocks a project until given date",
        requestBody = RequestBody(
            required = true,
            content = [
                Content(
                    mediaType = APPLICATION_JSON,
                    schema = Schema(
                        implementation = BlockProjectRequest::class,
                    )
                )
            ]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                content = [
                    Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ProjectResponseDTO::class))
                ]
            ), ApiResponse(
                responseCode = "400",
                content = [
                    Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorResponse::class))
                ]
            )
        ]
    )
    @Post("/{projectId}/block")
    fun blockProjectById(projectId: Long, @Body blockProjectRequest: BlockProjectRequest): ProjectResponseDTO =
        blockProjectByIdUseCase.blockProject(projectId, blockProjectRequest.blockDate)

    @Operation(summary = "Unblocks a project from a project ID")
    @Post("/{projectId}/unblock")
    fun unblockProjectById(projectId: Long): ProjectResponseDTO = unblockProjectByIdUseCase.unblockProject(projectId)
}