package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.entities.dto.ProjectResponseDTO
import com.autentia.tnt.binnacle.usecases.ProjectByIdUseCase
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.swagger.v3.oas.annotations.Operation

@Controller("/api/project")
internal class ProjectController private constructor(
    private val projectByIdUseCase: ProjectByIdUseCase
) {
    @Operation(summary = "Retrieves a project´s information from its ID")
    @Get("/{id}")
    fun getProjectById(id: Long): ProjectResponseDTO {
        return projectByIdUseCase.get(id)
    }
}