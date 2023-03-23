package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.core.domain.ProjectRoleRecent
import com.autentia.tnt.binnacle.core.domain.ProjectRoleResponse
import com.autentia.tnt.binnacle.entities.ProjectRole
import io.micronaut.data.annotation.Query
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository
import java.time.LocalDateTime

@Repository
internal interface ProjectRoleRepository : CrudRepository<ProjectRole, Long> {

    fun getAllByProjectId(id: Long): List<ProjectRole>

    fun getAllByIdIn(ids: List<Long>): List<ProjectRole>

    @Query("SELECT new com.autentia.tnt.binnacle.core.domain.ProjectRoleRecent(pr.id, pr.name, pr.project.name, pr.project.organization.name, pr.project.billable, pr.project.open, ac.start, pr.requireEvidence) FROM ProjectRole pr LEFT JOIN Activity ac ON pr.id = ac.projectRole.id WHERE ac.userId = :userId AND ac.start BETWEEN :startDate AND :endDate")
    fun findDistinctRolesBetweenDate(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        userId: Long
    ): List<ProjectRoleRecent>

    @Query("SELECT new com.autentia.tnt.binnacle.core.domain.ProjectRoleResponse(pr.id, pr.name, pr.project.organization.id, pr.project.id, pr.maxAllowed, pr.timeUnit, pr.requireEvidence, pr.isApprovalRequired, ac.userId) FROM ProjectRole pr LEFT JOIN Activity ac ON pr.id = ac.projectRole.id WHERE ac.userId IN :userIds")
    fun findDistinctRolesByUserIds(
        userIds: List<Long>
    ): List<ProjectRoleResponse>

}
