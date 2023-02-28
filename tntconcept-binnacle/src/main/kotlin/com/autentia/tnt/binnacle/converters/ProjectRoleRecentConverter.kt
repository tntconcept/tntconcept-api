package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.ProjectRoleRecent
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleRecentDTO
import jakarta.inject.Singleton

@Singleton
class ProjectRoleRecentConverter {

    fun toProjectRoleRecentDTO(projectRoleRecent: ProjectRoleRecent): ProjectRoleRecentDTO = ProjectRoleRecentDTO(
        id = projectRoleRecent.id,
        name = projectRoleRecent.name,
        projectName = projectRoleRecent.projectName,
        organizationName = projectRoleRecent.organizationName,
        projectBillable = projectRoleRecent.projectBillable,
        projectOpen = projectRoleRecent.projectOpen,
        date = projectRoleRecent.date,
        requireEvidence = projectRoleRecent.requireEvidence
    )

}
