package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.core.domain.ActivityTimeOnly
import com.autentia.tnt.binnacle.daos.ActivityDao
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.security.application.checkAuthentication
import com.autentia.tnt.security.application.id
import com.autentia.tnt.security.application.isAdmin
import io.micronaut.security.utils.SecurityService
import jakarta.inject.Singleton
import java.time.LocalDateTime

@Singleton
internal class ActivityRepositorySecured(
    private val activityDao: ActivityDao,
    private val securityService: SecurityService,
) : ActivityRepository {

    override fun findById(id: Long): Activity? {
        val authentication = securityService.checkAuthentication()

        return if (authentication.isAdmin()) {
            activityDao.findById(id).orElse(null)
        } else {
            activityDao.findByIdAndUserId(id, authentication.id())
        }
    }

    override fun find(startDate: LocalDateTime, endDate: LocalDateTime): List<Activity> {
        val authentication = securityService.checkAuthentication()
        return activityDao.getActivitiesBetweenDate(startDate, endDate, authentication.id())
    }

    override fun findWorkedMinutes(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<ActivityTimeOnly> {
        val authentication = securityService.checkAuthentication()
        return activityDao.workedMinutesBetweenDate(startDate, endDate, authentication.id())
    }

    override fun save(activity: Activity): Activity {
        val authentication = securityService.checkAuthentication()
        require(activity.userId == authentication.id()) { "Activity user id should be the same as authenticated user" }

        return activityDao.save(activity)
    }

    override fun update(activity: Activity): Activity {
        val authentication = securityService.checkAuthentication()
        require(activity.userId == authentication.id()) { "Activity user id should be the same as authenticated user" }

        val activityToUpdate = activityDao.findById(activity.id)
        require(activityToUpdate.isPresent) { "Activity to update does not exist" }

        return activityDao.update(activity)
    }

    override fun deleteById(id: Long) {
        val authentication = securityService.checkAuthentication()
        val activityToDelete = activityDao.findById(id)

        require(activityToDelete.isPresent) { "Activity with id $id does not exist" }
        require(activityToDelete.get().userId == authentication.id()) { "Activity user id should be the same as authenticated user" }

        activityDao.deleteById(id)
    }

}