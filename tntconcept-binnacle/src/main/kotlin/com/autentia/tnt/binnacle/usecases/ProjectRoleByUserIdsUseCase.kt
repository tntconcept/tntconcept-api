package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ProjectRoleResponseConverter
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleResponseDTO
import com.autentia.tnt.binnacle.services.ProjectRoleService
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
class ProjectRoleByUserIdsUseCase internal constructor(
    private val projectRoleService: ProjectRoleService,
    private val projectRoleResponseConverter: ProjectRoleResponseConverter
) {

    @Transactional
    @ReadOnly
    fun get(userIds: List<Long>): List<ProjectRoleResponseDTO> =

        projectRoleService.getByUserIds(userIds)
            .map { projectRoleResponseConverter.toProjectRoleResponseDTO(it) }
            .distinct()



}
