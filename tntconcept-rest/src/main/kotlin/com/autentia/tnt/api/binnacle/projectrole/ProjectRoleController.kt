package com.autentia.tnt.api.binnacle.projectrole

import com.autentia.tnt.binnacle.usecases.LatestProjectRolesForAuthenticatedUserUseCase
import com.autentia.tnt.binnacle.usecases.ProjectRoleByIdUseCase
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.swagger.v3.oas.annotations.Operation

@Controller("/api/project-role")
internal class ProjectRoleController(
    private val projectRoleByIdUseCase: ProjectRoleByIdUseCase,
    private val latestProjectRolesForAuthenticatedUserUseCase: LatestProjectRolesForAuthenticatedUserUseCase,
) {

    @Operation(summary = "Retrieves a project role by a given ID")
    @Get("/{id}")
    fun getProjectRoleById(id: Long): ProjectRoleResponse =
        ProjectRoleResponse.from(projectRoleByIdUseCase.get(id))

    @Operation(summary = "Retrieves recent used roles")
    @Get("/latest")
    fun getLatestRoles(@QueryValue year: Int?): List<ProjectRoleUserResponse> =
        latestProjectRolesForAuthenticatedUserUseCase.get(year).map { ProjectRoleUserResponse.from(it) }
}
