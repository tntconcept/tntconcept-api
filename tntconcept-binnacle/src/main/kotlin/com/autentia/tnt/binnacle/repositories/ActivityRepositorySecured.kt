package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.core.domain.ActivityTimeOnly
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates
import com.autentia.tnt.binnacle.repositories.predicates.PredicateBuilder
import com.autentia.tnt.security.application.canAccessAllActivities
import com.autentia.tnt.security.application.checkAuthentication
import com.autentia.tnt.security.application.id
import io.micronaut.context.annotation.Primary
import io.micronaut.data.jpa.repository.criteria.Specification
import io.micronaut.security.utils.SecurityService
import jakarta.inject.Singleton
import java.time.LocalDateTime

@Singleton
@Primary
internal class ActivityRepositorySecured(
        private val internalActivityRepository: InternalActivityRepository,
        private val securityService: SecurityService,
) : ActivityRepository {

    override fun findAll(activitySpecification: Specification<Activity>): List<Activity> =
            internalActivityRepository.findAll(addUserFilterIfNecessary(activitySpecification))

    override fun findById(id: Long): Activity? {
        val authentication = securityService.checkAuthentication()

        return if (authentication.canAccessAllActivities()) {
            internalActivityRepository.findById(id)
        } else {
            internalActivityRepository.findByIdAndUserId(id, authentication.id())
        }
    }

    override fun findByUserId(startDate: LocalDateTime, endDate: LocalDateTime, userId: Long): List<Activity> {
        val authentication = securityService.checkAuthentication()
        if (!authentication.canAccessAllActivities()) {
            require(userId == authentication.id()) { "User cannot get activities" }
        }
        return internalActivityRepository.findByUserId(startDate, endDate, userId)
    }


    override fun findByProjectRoleIdAndUserId(projectRoleId: Long, userId: Long): List<Activity> {
        val authentication = securityService.checkAuthentication()
        if (!authentication.canAccessAllActivities()) {
            require(authentication.id() == userId) { "User cannot get activities" }
        }
        return internalActivityRepository.findByProjectRoleIdAndUserId(projectRoleId, userId)
    }

    override fun find(startDate: LocalDateTime, endDate: LocalDateTime, userIds: List<Long>): List<Activity> {
        val authentication = securityService.checkAuthentication()
        val userIdsFiltered = if (!authentication.canAccessAllActivities()) {
            userIds.filter { it == authentication.id() }
        } else {
            userIds
        }

        return internalActivityRepository.find(startDate, endDate, userIdsFiltered)
    }

    override fun findOfLatestProjects(start: LocalDateTime, end: LocalDateTime, userId: Long): List<Activity> {
        val authentication = securityService.checkAuthentication()
        if (!authentication.canAccessAllActivities()) {
            require(authentication.id() == userId) { "User cannot get activities" }
        }
        return internalActivityRepository.findOfLatestProjects(start, end, userId)

    }

    override fun findByProjectId(
            start: LocalDateTime,
            end: LocalDateTime,
            projectId: Long,
            userId: Long
    ): List<Activity> {
        val authentication = securityService.checkAuthentication()
        if (!authentication.canAccessAllActivities()) {
            require(authentication.id() == userId) { "User cannot get activities" }
        }
        return internalActivityRepository.findByProjectId(start, end, projectId, userId)

    }

    @Deprecated("Use findIntervals function instead")
    override fun findWorkedMinutes(
            startDate: LocalDateTime,
            endDate: LocalDateTime,
            userId: Long
    ): List<ActivityTimeOnly> {
        val authentication = securityService.checkAuthentication()
        if (!authentication.canAccessAllActivities()) {
            require(authentication.id() == userId) { "User cannot get activities" }
        }
        return internalActivityRepository.findWorkedMinutes(startDate, endDate, userId)
    }

    override fun findOverlapped(startDate: LocalDateTime, endDate: LocalDateTime, userId: Long): List<Activity> {
        val authentication = securityService.checkAuthentication()
        if (!authentication.canAccessAllActivities()) {
            require(authentication.id() == userId) { "User cannot get overlapped activities" }
        }
        return internalActivityRepository.findOverlapped(startDate, endDate, userId)
    }

    override fun findByProjectRoleIds(
            start: LocalDateTime,
            end: LocalDateTime,
            projectRoleIds: List<Long>,
            userId: Long
    ): List<Activity> {
        val authentication = securityService.checkAuthentication()
        if (!authentication.canAccessAllActivities()) {
            require(authentication.id() == userId) { "User cannot get activities" }
        }
        return internalActivityRepository.findByProjectRoleIds(start, end, projectRoleIds, userId)
    }

    override fun save(activity: Activity): Activity {
        val authentication = securityService.checkAuthentication()
        require(activity.userId == authentication.id()) { "User cannot save activity" }

        return internalActivityRepository.save(activity)
    }

    override fun update(activity: Activity): Activity {
        val authentication = securityService.checkAuthentication()

        if (!authentication.canAccessAllActivities()) {
            require(activity.userId == authentication.id()) { "User cannot update activity" }
        }

        val activityToUpdate = activity.id?.let { internalActivityRepository.findById(it) }

        require(activityToUpdate != null) { "Activity to update does not exist" }

        return internalActivityRepository.update(activity)
    }

    override fun deleteById(id: Long) {
        val authentication = securityService.checkAuthentication()
        val activityToDelete = internalActivityRepository.findById(id)

        require(activityToDelete != null) { "Activity with id $id does not exist" }

        if (!authentication.canAccessAllActivities()) {
            require(activityToDelete.userId == authentication.id()) { "User cannot delete activity" }
        }

        internalActivityRepository.deleteById(id)
    }
    override fun deleteByIdWithoutSecurity(id: Long){
        internalActivityRepository.deleteById(id)
    }

    private fun addUserFilterIfNecessary(activitySpecification: Specification<Activity>): Specification<Activity> {
        val authentication = securityService.checkAuthentication()
        return if (authentication.canAccessAllActivities()) {
            activitySpecification
        } else {
            PredicateBuilder.and(activitySpecification, ActivityPredicates.userId(authentication.id()))
        }
    }
}