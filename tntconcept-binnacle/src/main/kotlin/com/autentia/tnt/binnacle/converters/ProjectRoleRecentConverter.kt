package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.ProjectRolesRecent
import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleRecentDTO
import jakarta.inject.Singleton

@Deprecated("Use ProjectRoleUser instead")
@Singleton
class ProjectRoleRecentConverter {

    fun toProjectRoleRecentDTO(projectRole: ProjectRolesRecent): ProjectRoleRecentDTO = ProjectRoleRecentDTO(
        id = projectRole.id,
        name = projectRole.name,
        projectName = projectRole.projectName,
        organizationName = projectRole.organizationName,
        projectBillable = projectRole.projectBillable,
        projectOpen = projectRole.projectOpen,
        date = projectRole.date,
        requireEvidence = setRequireEvidence(projectRole.requireEvidence)
    )

    fun setRequireEvidence(requireEvidence: RequireEvidence): Boolean{
        return requireEvidence != RequireEvidence.NO
    }

}