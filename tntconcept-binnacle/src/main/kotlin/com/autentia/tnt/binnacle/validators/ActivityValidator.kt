package com.autentia.tnt.binnacle.validators

import com.autentia.tnt.binnacle.core.domain.Activity
import com.autentia.tnt.binnacle.core.domain.Project
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.core.domain.User
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.exception.*
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.services.ProjectRoleService
import com.autentia.tnt.binnacle.services.ProjectService
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import java.time.LocalDateTime
import javax.transaction.Transactional

@Singleton
internal class ActivityValidator(
    private val activityService: ActivityService,
    private val activityCalendarService: ActivityCalendarService,
    private val projectRoleService: ProjectRoleService,
    private val projectService: ProjectService,
) {
    @Transactional
    @ReadOnly
    fun checkActivityIsValidForCreation(activityToCreate: Activity, user: User) {
        require(activityToCreate.id == null) { "Cannot create a new activity with id ${activityToCreate.id}." }

        checkProjectRoleExists(activityToCreate.projectRole.id)
        val project = projectService.findById(activityToCreate.projectRole.project.id)
        when {
            !isProjectOpen(project) -> throw ProjectClosedException()
            !isOpenPeriod(activityToCreate.timeInterval.start) -> throw ActivityPeriodClosedException()
            isProjectBlocked(project, activityToCreate) -> throw ActivityForBlockedProjectException()
            isOverlappingAnotherActivityTime(activityToCreate, user.id) -> throw OverlapsAnotherTimeException()
            user.isBeforeHiringDate(activityToCreate.timeInterval.start.toLocalDate()) ->
                throw ActivityBeforeHiringDateException()

            activityToCreate.isMoreThanOneDay() && activityToCreate.timeUnit === TimeUnit.MINUTES -> throw ActivityPeriodNotValidException()
        }
        checkIfIsExceedingMaxHoursForRole(
            Activity.emptyActivity(activityToCreate.projectRole, user),
            activityToCreate,
            user.id
        )
    }

    private fun checkIfIsExceedingMaxHoursForRole(currentActivity: Activity, activityToUpdate: Activity, userId: Long) {
        checkIfIsExceedingMaxHoursForRole(
            currentActivity, activityToUpdate, activityToUpdate.getYearOfStart(), userId
        )
        if (activityToUpdate.getYearOfStart() != activityToUpdate.getYearOfEnd()) {
            checkIfIsExceedingMaxHoursForRole(
                currentActivity, activityToUpdate, activityToUpdate.timeInterval.end.year, userId
            )
        }
    }

    private fun checkIfIsExceedingMaxHoursForRole(
        currentActivity: Activity,
        activityToUpdate: Activity,
        year: Int,
        userId: Long,
    ) {
        if (activityToUpdate.projectRole.maxAllowed > 0) {

            val yearTimeInterval = TimeInterval.ofYear(year)

            val yearCalendar = activityCalendarService.createCalendar(yearTimeInterval.getDateInterval())

            val currentActivityDuration = currentActivity.getDurationByCountingWorkableDays(yearCalendar)
            val activityToUpdateDuration = activityToUpdate.getDurationByCountingWorkableDays(yearCalendar)
            val activities =
                activityService.getActivitiesByProjectRoleIds(
                    yearTimeInterval,
                    listOf(activityToUpdate.projectRole.id),
                    userId
                )
            val totalRegisteredDurationForThisRole =
                activities.sumOf { it.getDurationByCountingWorkableDays(yearCalendar) }

            var totalRegisteredDurationForThisRoleAfterDiscount = totalRegisteredDurationForThisRole

            if (currentActivity.projectRole.id == activityToUpdate.projectRole.id) {
                totalRegisteredDurationForThisRoleAfterDiscount =
                    totalRegisteredDurationForThisRole - currentActivityDuration
            }

            val totalRegisteredDurationAfterSaveRequested =
                totalRegisteredDurationForThisRoleAfterDiscount + activityToUpdateDuration

            if (totalRegisteredDurationAfterSaveRequested > activityToUpdate.projectRole.maxAllowed) {
                val remainingTime =
                    (activityToUpdate.projectRole.maxAllowed - totalRegisteredDurationForThisRole.toDouble()) / DECIMAL_HOUR
                throw MaxHoursPerRoleException(
                    activityToUpdate.projectRole.maxAllowed / DECIMAL_HOUR,
                    remainingTime,
                    year
                )
            }
        }
    }

    private fun isOpenPeriod(startDate: LocalDateTime): Boolean {
        return startDate.year >= LocalDateTime.now().year - 1
    }

    @Transactional
    @ReadOnly
    fun checkActivityIsValidForUpdate(
        activityToUpdate: Activity,
        currentActivity: Activity,
        user: User,
    ) {
        require(activityToUpdate.id != null) { "Cannot update an activity without id." }
        checkProjectRoleExists(activityToUpdate.projectRole.id)
        val projectToUpdate = projectService.findById(activityToUpdate.projectRole.project.id)
        val currentProject = projectService.findById(currentActivity.projectRole.project.id)
        when {
            isProjectBlocked(projectToUpdate, activityToUpdate) -> throw ProjectBlockedException()
            isProjectBlocked(currentProject, currentActivity) -> throw ProjectBlockedException()
            !activityToUpdate.projectRole.project.open -> throw ProjectClosedException()
            !isOpenPeriod(activityToUpdate.timeInterval.start) -> throw ActivityPeriodClosedException()
            isOverlappingAnotherActivityTime(activityToUpdate, user.id) -> throw OverlapsAnotherTimeException()
            user.isBeforeHiringDate(activityToUpdate.timeInterval.start.toLocalDate()) ->
                throw ActivityBeforeHiringDateException()

            activityToUpdate.isMoreThanOneDay() && activityToUpdate.timeUnit === TimeUnit.MINUTES -> throw ActivityPeriodNotValidException()
        }
        checkIfIsExceedingMaxHoursForRole(currentActivity, activityToUpdate, user.id)
    }


    @Transactional
    @ReadOnly
    fun checkActivityIsValidForDeletion(id: Long) {
        val activity = activityService.getActivityById(id)
        val project = projectService.findById(activity.projectRole.project.id)
        when {
            isProjectBlocked(project, activity) -> throw ProjectBlockedException()
            !isOpenPeriod(activity.getStart()) -> throw ActivityPeriodClosedException()
        }
    }

    private fun isProjectOpen(project: Project): Boolean {
        return project.open
    }

    private fun isProjectBlocked(project: Project, activity: Activity): Boolean {
        if (project.blockDate == null) {
            return false
        }
        return project.blockDate.isAfter(
            activity.getStart().toLocalDate()
        ) || project.blockDate.isEqual(activity.getStart().toLocalDate())
    }

    private fun isOverlappingAnotherActivityTime(
        activity: Activity,
        userId: Long,
    ): Boolean {
        if (activity.duration == 0) {
            return false
        }
        val activities = activityService.findOverlappedActivities(activity.getStart(), activity.getEnd(), userId)
        return activities.size > 1 || activities.size == 1 && activities[0].id != activity.id
    }

    private fun checkProjectRoleExists(id: Long) {
        projectRoleService.getByProjectRoleId(id)
    }

    private companion object {
        private const val DECIMAL_HOUR = 60.0
    }
}