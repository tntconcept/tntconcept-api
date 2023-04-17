package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.core.domain.ActivityTimeOnly
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.security.application.checkAuthentication
import com.autentia.tnt.security.application.id
import com.autentia.tnt.security.application.isAdmin
import com.autentia.tnt.security.application.isNotAdmin
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
        return activityDao.find(startDate, endDate, authentication.id())
    }

    override fun findWithoutSecurity(startDate: LocalDateTime, endDate: LocalDateTime, userId: Long): List<Activity> {
        //TODO: Add security to this method!!!
        return activityDao.find(startDate, endDate, userId)
    }

    override fun find(approvalState: ApprovalState): List<Activity> {
        val authentication = securityService.checkAuthentication()
        return if (authentication.isAdmin()) {
           return activityDao.findByApprovalState(approvalState)
        } else {
            return activityDao.findByApprovalStateAndUserId(approvalState, authentication.id())
        }
    }

    override fun find(projectRoleId: Long): List<Activity> {
        val authentication = securityService.checkAuthentication()
        return activityDao.findByProjectRoleIdAndUserId(projectRoleId, authentication.id())
    }

    override fun find(startDate: LocalDateTime, endDate: LocalDateTime, userIds: List<Long>): List<Activity> {
        val authentication = securityService.checkAuthentication()
        val userIdsFiltered = if (!authentication.isAdmin()) {
            userIds.filter { it == authentication.id() }
        } else {
            userIds
        }

        return activityDao.find(startDate, endDate, userIdsFiltered)
    }

    override fun findOfLatestProjects(startDate: LocalDateTime, endDate: LocalDateTime): List<Activity> {
        val authentication = securityService.checkAuthentication()
        return activityDao.findOfLatestProjects(startDate, endDate, authentication.id())
    }

    override fun findWorkedMinutes(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<ActivityTimeOnly> {
        val authentication = securityService.checkAuthentication()
        return activityDao.findWorkedMinutes(startDate, endDate, authentication.id())
    }

    override fun findOverlapped(startDate: LocalDateTime, endDate: LocalDateTime): List<Activity> {
        val authentication = securityService.checkAuthentication()
        return activityDao.findOverlapped(startDate, endDate, authentication.id())
    }

    override fun find(start: LocalDateTime, end: LocalDateTime, projectRoleId: Long): List<Activity> {
        val authentication = securityService.checkAuthentication()
        return activityDao.find(start, end, projectRoleId, authentication.id())
    }

    override fun save(activity: Activity): Activity {
        val authentication = securityService.checkAuthentication()
        require(activity.userId == authentication.id()) { "User cannot save activity" }

        return activityDao.save(activity)
    }

    override fun update(activity: Activity): Activity {
        val authentication = securityService.checkAuthentication()

        if(authentication.isNotAdmin()) {
            require(activity.userId == authentication.id()) { "User cannot update activity" }
        }

        val activityToUpdate = activityDao.findById(activity.id)
        require(activityToUpdate.isPresent) { "Activity to update does not exist" }

        return activityDao.update(activity)
    }

    override fun deleteById(id: Long) {
        val authentication = securityService.checkAuthentication()
        val activityToDelete = activityDao.findById(id)

        require(activityToDelete.isPresent) { "Activity with id $id does not exist" }
        require(activityToDelete.get().userId == authentication.id()) { "User cannot delete activity" }

        activityDao.deleteById(id)
    }

}