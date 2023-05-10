package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.core.domain.ActivityInterval
import com.autentia.tnt.binnacle.core.domain.ActivityTimeOnly
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ApprovalState
import io.micronaut.data.jpa.repository.criteria.Specification
import jakarta.inject.Singleton
import java.time.LocalDateTime


@Singleton
internal class InternalActivityRepository(private val activityDao: ActivityDao) : ActivityRepository {
    override fun findAll(activitySpecification: Specification<Activity>): List<Activity> = activityDao.findAll(activitySpecification)

    override fun findById(id: Long): Activity? {
        throw NotImplementedError()
    }

    override fun findByIdWithoutSecurity(id: Long): Activity? {
        throw NotImplementedError()
    }

    override fun find(startDate: LocalDateTime, endDate: LocalDateTime): List<Activity> {
        throw NotImplementedError()
    }

    override fun find(approvalState: ApprovalState): List<Activity> {
        throw NotImplementedError()
    }

    override fun find(projectRoleId: Long): List<Activity> {
        throw NotImplementedError()
    }

    override fun findWithoutSecurity(startDate: LocalDateTime, endDate: LocalDateTime, userId: Long): List<Activity> {
        throw NotImplementedError()
    }

    override fun findWorkedMinutes(startDate: LocalDateTime, endDate: LocalDateTime): List<ActivityTimeOnly> {
        throw NotImplementedError()
    }

    override fun findOverlapped(startDate: LocalDateTime, endDate: LocalDateTime): List<Activity> {
        throw NotImplementedError()
    }

    override fun findIntervals(start: LocalDateTime, end: LocalDateTime, projectRoleId: Long): List<ActivityInterval> {
        throw NotImplementedError()
    }

    override fun save(activity: Activity): Activity {
        throw NotImplementedError()
    }

    override fun saveWithoutSecurity(activity: Activity): Activity {
        throw NotImplementedError()
    }

    override fun update(activity: Activity): Activity {
        throw NotImplementedError()
    }

    override fun updateWithoutSecurity(activity: Activity): Activity {
        throw NotImplementedError()
    }

    override fun deleteById(id: Long) {
        throw NotImplementedError()
    }
}