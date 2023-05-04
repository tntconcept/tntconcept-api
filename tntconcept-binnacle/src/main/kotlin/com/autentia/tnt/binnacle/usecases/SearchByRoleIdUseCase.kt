package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ProjectRoleResponseConverter
import com.autentia.tnt.binnacle.converters.SearchConverter
import com.autentia.tnt.binnacle.entities.dto.SearchResponseDTO
import com.autentia.tnt.binnacle.services.ProjectRoleService
import com.autentia.tnt.security.application.checkAuthentication
import com.autentia.tnt.security.application.id
import io.micronaut.security.utils.SecurityService
import jakarta.inject.Singleton

@Singleton
class SearchByRoleIdUseCase internal constructor(
    private val projectRoleService: ProjectRoleService,
    private val securityService: SecurityService,
    private val searchConverter: SearchConverter,
    private val projectRoleResponseConverter: ProjectRoleResponseConverter
) {

    fun getDescriptions(roleIds: List<Long>): SearchResponseDTO {

        val authentication = securityService.checkAuthentication()

        // Remove duplicated elements to search
        val distinctRoles = roleIds.distinct()

        val roles = projectRoleService.getAllByIds(distinctRoles.map(Long::toInt))

        val projectRoleUsers = roles.map { projectRole ->
            projectRoleResponseConverter.toProjectRoleUser(projectRole, authentication.id())
        }

        return searchConverter.toResponseDTO(roles, projectRoleUsers)
    }
}