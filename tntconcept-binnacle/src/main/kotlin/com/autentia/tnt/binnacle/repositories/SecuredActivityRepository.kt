package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.daos.ActivityDao
import com.autentia.tnt.binnacle.entities.Activity
import io.micronaut.security.utils.SecurityService

internal class SecuredActivityRepository(
    private val activityDao: ActivityDao,
    private val securityService: SecurityService
) {

    fun findById(id: Long, userId: Long): Activity? {
        return activityDao.findByIdAndUserId(id, userId)
    }

}