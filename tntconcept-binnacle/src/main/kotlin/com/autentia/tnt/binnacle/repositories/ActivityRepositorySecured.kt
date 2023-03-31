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
): ActivityRepository {

    override fun findById(id: Long): Activity? {
        val authentication = securityService.checkAuthentication()

        return if(authentication.isAdmin()){
            activityDao.findById(id).orElse(null)
        } else{
            activityDao.findByIdAndUserId(id, authentication.id())
        }
    }

    override fun find(startDate: LocalDateTime, endDate: LocalDateTime, userId: Long): List<Activity> {
        val authentication = securityService.checkAuthentication()

        if(userId != authentication.id()){
            return if (authentication.isAdmin()) {
                activityDao.getActivitiesBetweenDate(startDate, endDate, userId)
            } else {
                emptyList()
            }
        }
        return activityDao.getActivitiesBetweenDate(startDate, endDate, userId)
    }

    override fun findWorkedMinutes(startDate: LocalDateTime, endDate: LocalDateTime, userId: Long): List<ActivityTimeOnly>{
        val authentication = securityService.checkAuthentication()

        if(userId != authentication.id()){
            return if (authentication.isAdmin()) {
                activityDao.workedMinutesBetweenDate(startDate, endDate, userId)
            } else {
                emptyList()
            }
        }
        return activityDao.workedMinutesBetweenDate(startDate, endDate, userId)
    }

    override fun save(activity: Activity): Activity {
        //TODO: check userId is the same of the authenticated used
        return activityDao.save(activity)
    }

    override fun update(activity: Activity): Activity {
        //TODO: check userId is the same of the authenticated used
        return activityDao.update(activity)
    }

    override fun deleteById(id: Long) {
        //TODO: check userId is the same of the authenticated used (Use Kotlin require)
        activityDao.deleteById(id)
    }

}