package com.autentia.tnt.binnacle.validators

import com.autentia.tnt.binnacle.core.domain.ActivityRequestBody
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.exception.ActivityBeforeHiringDateException
import com.autentia.tnt.binnacle.exception.ActivityNotFoundException
import com.autentia.tnt.binnacle.exception.ActivityPeriodClosedException
import com.autentia.tnt.binnacle.exception.MaxHoursPerRoleException
import com.autentia.tnt.binnacle.exception.OverlapsAnotherTimeException
import com.autentia.tnt.binnacle.exception.ProjectClosedException
import com.autentia.tnt.binnacle.exception.ProjectRoleNotFoundException
import com.autentia.tnt.binnacle.exception.UserPermissionException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import java.time.LocalDate
import java.time.LocalDateTime
import javax.transaction.Transactional

@Singleton
internal class ActivityValidator(
    private val activityRepository: ActivityRepository,
    private val activityCalendarService: ActivityCalendarService,
    private val projectRoleRepository: ProjectRoleRepository
) {
    @Transactional
    @ReadOnly
    fun checkActivityIsValidForCreation(activityRequest: ActivityRequestBody, user: User) {
        require(activityRequest.id == null) { "Cannot create a new activity with id ${activityRequest.id}." }

        val projectRoleDb = projectRoleRepository.findById(activityRequest.projectRoleId).orElse(null)
        when {
            projectRoleDb === null -> throw ProjectRoleNotFoundException(activityRequest.projectRoleId)
            !isProjectOpen(projectRoleDb.project) -> throw ProjectClosedException()
            !isOpenPeriod(activityRequest.start) -> throw ActivityPeriodClosedException()
            isOverlappingAnotherActivityTime(activityRequest, user) -> throw OverlapsAnotherTimeException()
            isBeforeHiringDate(
                activityRequest.start.toLocalDate(),
                user
            ) -> throw ActivityBeforeHiringDateException()

        }
        checkIfIsExceedingMaxHoursForRole(Activity.emptyActivity(projectRoleDb), activityRequest, projectRoleDb, user)

    }

    private fun checkIfIsExceedingMaxHoursForRole(
        currentActivity: Activity,
        requestActivity: ActivityRequestBody,
        projectRole: ProjectRole,
        user: User
    ) {
        checkIfIsExceedingMaxHoursForRole(
            currentActivity, requestActivity, requestActivity.start.year, projectRole, user
        )
        if (requestActivity.start.year != requestActivity.end.year) {
            checkIfIsExceedingMaxHoursForRole(
                currentActivity, requestActivity, requestActivity.end.year, projectRole, user
            )
        }
    }


    private fun checkIfIsExceedingMaxHoursForRole(
        currentActivity: Activity,
        requestActivity: ActivityRequestBody,
        year: Int,
        projectRole: ProjectRole,
        user: User
    ) {
        if (projectRole.maxAllowed > 0) {

            val yearTimeInterval = TimeInterval.ofYear(year)

            val calendar = activityCalendarService.createCalendar(yearTimeInterval.getDateInterval())

            val currentActivityDuration = activityCalendarService.getDurationByCountingWorkingDays(
                calendar, currentActivity.getTimeInterval(), currentActivity.projectRole
            )

            val requestActivityDuration = activityCalendarService.getDurationByCountingWorkingDays(
                calendar, requestActivity.getTimeInterval(), projectRole
            )

            val totalRegisteredDurationForThisRole =
                activityCalendarService.getSumActivitiesDuration(
                    calendar, yearTimeInterval, projectRole.id, user.id
                )

            var totalRegisteredDurationForThisRoleAfterDiscount = totalRegisteredDurationForThisRole

            if (currentActivity.projectRole.id == requestActivity.projectRoleId) {
                totalRegisteredDurationForThisRoleAfterDiscount =
                    totalRegisteredDurationForThisRole - currentActivityDuration
            }

            val totalRegisteredDurationAfterSaveRequested =
                totalRegisteredDurationForThisRoleAfterDiscount + requestActivityDuration

            if (totalRegisteredDurationAfterSaveRequested > projectRole.maxAllowed) {
                val remainingTime =
                    (projectRole.maxAllowed - totalRegisteredDurationForThisRole.toDouble()) / DECIMAL_HOUR
                throw MaxHoursPerRoleException(projectRole.maxAllowed / DECIMAL_HOUR, remainingTime, year)
            }
        }
    }

    private fun isBeforeHiringDate(startDate: LocalDate, user: User): Boolean {
        return startDate.isBefore(user.hiringDate)
    }

    private fun isOpenPeriod(startDate: LocalDateTime): Boolean {
        return startDate.year >= LocalDateTime.now().year - 1
    }

    @Transactional
    @ReadOnly
    fun checkActivityIsValidForUpdate(activityRequest: ActivityRequestBody, user: User) {
        require(activityRequest.id != null) { "Cannot update an activity without id." }

        val activityDb = activityRepository.findById(activityRequest.id).orElse(null)
        val projectRoleDb = projectRoleRepository.findById(activityRequest.projectRoleId).orElse(null)
        when {
            activityDb == null -> throw ActivityNotFoundException(activityRequest.id!!)
            projectRoleDb === null -> throw ProjectRoleNotFoundException(activityRequest.projectRoleId)
            !userHasAccess(activityDb, user) -> throw UserPermissionException()
            !isProjectOpen(projectRoleDb.project) -> throw ProjectClosedException()
            !isOpenPeriod(activityRequest.start) -> throw ActivityPeriodClosedException()
            isOverlappingAnotherActivityTime(activityRequest, user) -> throw OverlapsAnotherTimeException()

            isBeforeHiringDate(
                activityRequest.start.toLocalDate(),
                user
            ) -> throw ActivityBeforeHiringDateException()
        }
        checkIfIsExceedingMaxHoursForRole(activityDb, activityRequest, projectRoleDb, user)
    }


    @Transactional
    @ReadOnly
    fun checkActivityIsValidForDeletion(id: Long, user: User) {
        val activityDb = activityRepository.findById(id).orElse(null)
        when {
            activityDb === null -> throw ActivityNotFoundException(id)
            !isOpenPeriod(activityDb.start) -> throw ActivityPeriodClosedException()
            !userHasAccess(activityDb, user) -> throw UserPermissionException()
        }
    }

    @Transactional
    @ReadOnly
    fun checkIfUserCanApproveActivity(user: User, activityId: Long): Boolean{
        //TODO: Use JWT to know if user have staff role
        val activity = activityRepository.findById(activityId).orElse(null)
        when {
            activity === null -> throw ActivityNotFoundException(activityId)
            !userHasAccess(activity, user) -> throw UserPermissionException()
        }
        return true
    }

    fun userHasAccess(activityDb: Activity, user: User): Boolean {
        return activityDb.userId == user.id
    }

    fun isProjectOpen(project: Project): Boolean {
        return project.open
    }

    private fun isOverlappingAnotherActivityTime(
        activityRequest: ActivityRequestBody,
        user: User
    ): Boolean {
        if (activityRequest.duration == 0) {
            return false
        }
        val activities =
            activityRepository.getOverlappingActivities(
                activityRequest.start, activityRequest.end, user.id
            )
        return activities.size > 1 || activities.size == 1 && activities[0].id != activityRequest.id
    }

    private companion object {
        private const val DECIMAL_HOUR = 60.0
    }
}