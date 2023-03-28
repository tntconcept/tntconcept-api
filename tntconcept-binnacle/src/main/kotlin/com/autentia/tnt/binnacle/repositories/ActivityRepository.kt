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
internal class ActivityRepository(
    private val activityDao: ActivityDao,
    private val securityService: SecurityService,
) {

    fun findById(id: Long): Activity? {
        val authentication = securityService.checkAuthentication()

        return if(authentication.isAdmin()){
            activityDao.findById(id).orElse(null)
        } else{
            activityDao.findByIdAndUserId(id, authentication.id())
        }
    }

    fun getActivitiesBetweenDate(startDate: LocalDateTime, endDate: LocalDateTime, userId: Long): List<Activity> {
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

    fun workedMinutesBetweenDate(startDate: LocalDateTime, endDate: LocalDateTime, userId: Long): List<ActivityTimeOnly>{
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

    fun save(activity: Activity): Activity {
        return activityDao.save(activity)
    }

    fun update(activity: Activity): Activity {
        return activityDao.update(activity)
    }

    fun deleteById(id: Long) {
        activityDao.deleteById(id)
    }

}