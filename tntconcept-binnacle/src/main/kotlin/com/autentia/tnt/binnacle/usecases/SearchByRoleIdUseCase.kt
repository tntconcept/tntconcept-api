package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.SearchConverter
import com.autentia.tnt.binnacle.entities.dto.SearchResponseDTO
import com.autentia.tnt.binnacle.services.ProjectRoleService
import jakarta.inject.Singleton

@Singleton
class SearchByRoleIdUseCase internal constructor(
    private val projectRoleService: ProjectRoleService,
    private val searchConverter: SearchConverter
) {

    fun getDescriptions(roleIds: List<Long>): SearchResponseDTO {
        // Remove duplicated elements to search
        val distinctRoles = roleIds.distinct()

        val roles = projectRoleService.getAllByProjectIds(distinctRoles.map(Long::toInt))

        return searchConverter.toResponseDTO(roles)
    }

}
