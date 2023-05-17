package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.core.domain.ActivityTimeOnly
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ApprovalState
import io.micronaut.data.jpa.repository.criteria.Specification
import jakarta.inject.Singleton
import java.time.LocalDateTime
import java.util.Optional


@Singleton
internal class InternalActivityRepository(private val activityDao: ActivityDao) : ActivityRepository {

    override fun findAll(activitySpecification: Specification<Activity>): List<Activity> =
        activityDao.findAll(activitySpecification)

    override fun findById(id: Long): Activity? {
        val activity: Optional<Activity> = activityDao.findById(id)
        return if (activity.isPresent) {
            activity.get()
        } else {
            null
        }
    }

    override fun find(startDate: LocalDateTime, endDate: LocalDateTime, userIds: List<Long>): List<Activity> {
        return activityDao.find(startDate, endDate, userIds)
    }

    override fun find(approvalState: ApprovalState): List<Activity> {
        throw NotImplementedError()
    }

    override fun find(projectRoleId: Long): List<Activity> {
        throw NotImplementedError()
    }

    override fun findByUserId(startDate: LocalDateTime, endDate: LocalDateTime, userId: Long): List<Activity> {
        return activityDao.find(startDate, endDate, userId)
    }

    override fun findByProjectRoleIds(
        start: LocalDateTime,
        end: LocalDateTime,
        projectRoleIds: List<Long>,
        userId: Long
    ): List<Activity> {
        return activityDao.findByProjectRoleIds(start, end, projectRoleIds, userId)
    }

    override fun findOfLatestProjects(start: LocalDateTime, end: LocalDateTime): List<Activity> {
        throw NotImplementedError()
    }

    override fun findByProjectId(start: LocalDateTime, end: LocalDateTime, projectId: Long): List<Activity> {
        throw NotImplementedError()
    }

    override fun findWorkedMinutes(startDate: LocalDateTime, endDate: LocalDateTime): List<ActivityTimeOnly> {
        throw NotImplementedError()
    }

    override fun findOverlapped(startDate: LocalDateTime, endDate: LocalDateTime): List<Activity> {
        throw NotImplementedError()
    }

    fun findByIdAndUserId(id: Long, userId: Long): Activity? {
        TODO("Not yet implemented")
    }

    fun findByApprovalState(approvalState: ApprovalState): List<Activity> {
        TODO("Not yet implemented")
    }

    fun findByApprovalStateAndUserId(approvalState: ApprovalState, userId: Long): List<Activity> {
        TODO("Not yet implemented")
    }

    fun findByProjectRoleIdAndUserId(id: Long, userId: Long): List<Activity> {
        TODO("Not yet implemented")
    }

    fun findOfLatestProjects(startDate: LocalDateTime, endDate: LocalDateTime, userId: Long): List<Activity> {
        TODO("Not yet implemented")
    }

    fun findByProjectId(start: LocalDateTime, end: LocalDateTime, projectId: Long, id: Long): List<Activity> {
        TODO("Not yet implemented")
    }

    fun findWorkedMinutes(startDate: LocalDateTime?, endDate: LocalDateTime?, userId: Long): List<ActivityTimeOnly> {
        TODO("Not yet implemented")
    }

    fun findOverlapped(startDate: LocalDateTime, endDate: LocalDateTime, id: Long): List<Activity> {
        TODO("Not yet implemented")

    }

    override fun save(activity: Activity): Activity {
        throw NotImplementedError()
    }

    override fun update(activity: Activity): Activity {
        throw NotImplementedError()
    }

    override fun deleteById(id: Long) {
        throw NotImplementedError()
    }

}