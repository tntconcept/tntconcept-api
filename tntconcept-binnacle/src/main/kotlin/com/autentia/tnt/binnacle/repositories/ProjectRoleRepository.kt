package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.core.domain.ProjectRoleRecent
import com.autentia.tnt.binnacle.core.domain.ProjectRoleUser
import com.autentia.tnt.binnacle.core.domain.ProjectRolesRecent
import com.autentia.tnt.binnacle.entities.ProjectRole
import java.time.LocalDateTime

interface ProjectRoleRepository {

    fun findById(id: Long): ProjectRole?

    fun getAllByProjectId(id: Long): List<ProjectRole>

    fun getAllByProjectIdIn(ids: List<Long>): List<ProjectRole>

    fun getAllByIdIn(ids: List<Long>): List<ProjectRole>

    fun findDistinctRolesBetweenDate(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
    ): List<ProjectRoleRecent>

    @Deprecated("Use findDistinctRolesBetweenDate instead")
    fun findDistinctProjectRolesBetweenDate(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<ProjectRolesRecent>

    fun findDistinctRolesByUserIds(
        userIds: List<Long>
    ): List<ProjectRoleUser>

}
