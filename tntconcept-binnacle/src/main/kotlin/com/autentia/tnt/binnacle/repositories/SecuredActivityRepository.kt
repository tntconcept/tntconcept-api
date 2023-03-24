package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.daos.ActivityDao
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.security.application.checkAuthentication
import com.autentia.tnt.security.application.id
import com.autentia.tnt.security.application.isAdmin
import io.micronaut.security.utils.SecurityService
import java.time.LocalDateTime

internal class SecuredActivityRepository(
    private val activityDao: ActivityDao,
    private val securityService: SecurityService
) {

    fun findById(id: Long): Activity? {
        val authentication = securityService.checkAuthentication()

        return if(authentication.isAdmin()){
            activityDao.findById(id).orElse(null)
        } else{
            activityDao.findByIdAndUserId(id, authentication.name.toLong())
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

}