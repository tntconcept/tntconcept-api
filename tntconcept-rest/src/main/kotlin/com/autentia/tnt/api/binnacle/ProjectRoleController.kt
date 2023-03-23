package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.converters.ProjectRoleRecentConverter
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleRecentDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleResponseDTO
import com.autentia.tnt.binnacle.usecases.LatestProjectRolesForAuthenticatedUserUseCase
import com.autentia.tnt.binnacle.usecases.ProjectRoleByIdUseCase
import com.autentia.tnt.binnacle.usecases.ProjectRoleByUserIdsUseCase
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.swagger.v3.oas.annotations.Operation

@Controller("/api/project-role")
internal class ProjectRoleController(
    private val projectRoleByIdUseCase: ProjectRoleByIdUseCase,
    private val latestProjectRolesForAuthenticatedUserUseCase: LatestProjectRolesForAuthenticatedUserUseCase,
    private val projectRoleByUserIdsUseCase: ProjectRoleByUserIdsUseCase,
    private val projectRoleRecentConverter: ProjectRoleRecentConverter,
) {

    @Operation(summary = "Retrieves a project role by a given ID")
    @Get("/{id}")
    fun getProjectRoleById(id: Long): ProjectRoleDTO =
        projectRoleByIdUseCase.get(id)

    @Operation(summary = "Retrieves recent used roles")
    @Get("/recents")
    fun getLatestRoles(): List<ProjectRoleRecentDTO> =
        latestProjectRolesForAuthenticatedUserUseCase
            .get()
            .map { projectRoleRecentConverter.toProjectRoleRecentDTO(it) }

    @Operation(summary = "Retrieves a project roles list by given user IDs")
    @Get
    fun getProjectRoleByUserIds(userIds: List<Long>): List<ProjectRoleResponseDTO> =
        projectRoleByUserIdsUseCase.get(userIds)
}
