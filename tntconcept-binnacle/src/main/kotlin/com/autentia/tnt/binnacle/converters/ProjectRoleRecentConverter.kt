package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.ProjectRoleRecent
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleRecentDTO
import jakarta.inject.Singleton

@Singleton
class ProjectRoleRecentConverter {

    fun toProjectRoleRecentDTO(projectRoleRecent: ProjectRoleRecent): ProjectRoleRecentDTO = ProjectRoleRecentDTO(
        projectRoleRecent.id,
        projectRoleRecent.name,
        projectRoleRecent.projectName,
        projectRoleRecent.organizationName,
        projectRoleRecent.projectBillable,
        projectRoleRecent.projectOpen,
        projectRoleRecent.date,
        projectRoleRecent.requireEvidence
    )

}
