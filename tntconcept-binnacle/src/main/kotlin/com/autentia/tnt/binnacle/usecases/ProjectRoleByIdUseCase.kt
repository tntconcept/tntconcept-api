package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ProjectRoleResponseConverter
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleDTO
import com.autentia.tnt.binnacle.services.ProjectRoleService
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
class ProjectRoleByIdUseCase internal constructor(
    private val projectRoleService: ProjectRoleService,
    private val projectRoleResponseConverter: ProjectRoleResponseConverter,
) {
    @Transactional
    @ReadOnly
    fun get(id: Long): ProjectRoleDTO {
        val projectRole = projectRoleService.getByProjectRoleId(id)
        return projectRoleResponseConverter.toProjectRoleDTO(projectRole)
    }
}
