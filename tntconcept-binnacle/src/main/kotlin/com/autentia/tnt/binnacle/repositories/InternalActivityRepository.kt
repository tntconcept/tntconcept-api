package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.core.domain.ActivityTimeOnly
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ApprovalState
import io.micronaut.data.jpa.repository.criteria.Specification
import jakarta.inject.Singleton
import java.time.LocalDateTime
import java.util.*


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

    fun find(approvalState: ApprovalState): List<Activity> {
        return activityDao.findByApprovalState(approvalState)
    }

    fun findByApprovalStateAndUserId(approvalState: ApprovalState, userId: Long): List<Activity> {
        return activityDao.findByApprovalStateAndUserId(approvalState, userId)
    }

    override fun findByProjectRoleIdAndUserId(projectRoleId: Long, userId: Long): List<Activity> {
        return activityDao.findByProjectRoleIdAndUserId(projectRoleId, userId)
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

    override fun findOfLatestProjects(start: LocalDateTime, end: LocalDateTime, userId: Long): List<Activity> {
        return activityDao.findOfLatestProjects(start, end, userId)
    }

    override fun findByProjectId(
        start: LocalDateTime,
        end: LocalDateTime,
        projectId: Long,
        userId: Long
    ): List<Activity> {
        return activityDao.findByProjectId(start, end, projectId, userId)
    }

    @Deprecated("Use findIntervals function instead")
    override fun findWorkedMinutes(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        userId: Long
    ): List<ActivityTimeOnly> {
        return activityDao.findWorkedMinutes(startDate, endDate, userId)
    }

    override fun findOverlapped(startDate: LocalDateTime, endDate: LocalDateTime, userId: Long): List<Activity> {
        return activityDao.findOverlapped(startDate, endDate, userId)
    }

    fun findByIdAndUserId(id: Long, userId: Long): Activity? {
        return activityDao.findByIdAndUserId(id, userId)
    }

    override fun save(activity: Activity): Activity {
        return activityDao.save(activity)
    }

    override fun update(activity: Activity): Activity {
        return activityDao.update(activity)
    }

    override fun deleteById(id: Long) {
        activityDao.deleteById(id)
    }

}