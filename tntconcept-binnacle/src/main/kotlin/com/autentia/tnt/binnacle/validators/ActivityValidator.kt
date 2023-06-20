package com.autentia.tnt.binnacle.validators

import com.autentia.tnt.binnacle.core.domain.Activity
import com.autentia.tnt.binnacle.core.domain.Project
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.core.domain.User
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.exception.*
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.services.ProjectService
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import java.time.LocalDateTime
import javax.transaction.Transactional

@Singleton
internal class ActivityValidator(
    private val activityService: ActivityService,
    private val activityCalendarService: ActivityCalendarService,
    private val projectService: ProjectService,
) {
    @Transactional
    @ReadOnly
    fun checkActivityIsValidForCreation(activityToCreate: Activity, user: User) {
        require(activityToCreate.id == null) { "Cannot create a new activity with id ${activityToCreate.id}." }
        val project = projectService.findById(activityToCreate.projectRole.project.id)
        val emptyActivity = Activity.emptyActivity(activityToCreate.projectRole, user)
        val activityToCreateStartYear = activityToCreate.getYearOfStart()
        val activityToCreateEndYear = activityToCreate.timeInterval.end.year
        val totalRegisteredDurationForThisRoleStartYear =
            getTotalRegisteredDurationByProjectRole(emptyActivity, activityToCreateStartYear, user.id)
        val totalRegisteredDurationForThisRoleEndYear =
            getTotalRegisteredDurationByProjectRole(emptyActivity, activityToCreateEndYear, user.id)

        when {
            !isProjectOpen(project) -> throw ProjectClosedException()
            !isOpenPeriod(activityToCreate.timeInterval.start) -> throw ActivityPeriodClosedException()
            isProjectBlocked(project, activityToCreate) -> throw ProjectBlockedException(project.blockDate!!)
            isOverlappingAnotherActivityTime(activityToCreate, user.id) -> throw OverlapsAnotherTimeException()
            user.isBeforeHiringDate(activityToCreate.timeInterval.start.toLocalDate()) ->
                throw ActivityBeforeHiringDateException()

            activityToCreate.isMoreThanOneDay() && activityToCreate.timeUnit === TimeUnit.MINUTES -> throw ActivityPeriodNotValidException()

            isExceedingMaxHoursForRole(
                emptyActivity,
                activityToCreate,
                activityToCreateStartYear,
                totalRegisteredDurationForThisRoleStartYear
            ) -> throw MaxHoursPerRoleException(
                activityToCreate.projectRole.maxAllowed / DECIMAL_HOUR,
                getRemaining(
                    activityToCreate,
                    totalRegisteredDurationForThisRoleStartYear
                ),
                activityToCreateStartYear
            )

            (activityToCreateStartYear != activityToCreateEndYear) && isExceedingMaxHoursForRole(
                emptyActivity,
                activityToCreate,
                activityToCreateEndYear,
                totalRegisteredDurationForThisRoleEndYear
            ) -> throw MaxHoursPerRoleException(
                activityToCreate.projectRole.maxAllowed / DECIMAL_HOUR,
                getRemaining(
                    activityToCreate,
                    totalRegisteredDurationForThisRoleEndYear
                ),
                activityToCreateEndYear
            )
        }
    }

    private fun getTotalRegisteredDurationByProjectRole(
        activityToUpdate: Activity,
        year: Int,
        userId: Long,
    ): Int {
        val yearTimeInterval = TimeInterval.ofYear(year)

        val yearCalendar = activityCalendarService.createCalendar(yearTimeInterval.getDateInterval())

        val activities =
            activityService.getActivitiesByProjectRoleIds(
                yearTimeInterval,
                listOf(activityToUpdate.projectRole.id),
                userId
            )
        return activities.sumOf { it.getDurationByCountingWorkableDays(yearCalendar) }
    }

    private fun getTotalRegisteredDurationForThisRoleAfterSave(
        currentActivity: Activity,
        activityToUpdate: Activity,
        year: Int,
        totalRegisteredDurationForThisRole: Int,
    ): Int {
        val yearTimeInterval = TimeInterval.ofYear(year)
        val yearCalendar = activityCalendarService.createCalendar(yearTimeInterval.getDateInterval())
        val currentActivityDuration = currentActivity.getDurationByCountingWorkableDays(yearCalendar)
        val activityToUpdateDuration = activityToUpdate.getDurationByCountingWorkableDays(yearCalendar)

        var totalRegisteredDurationForThisRoleAfterDiscount = totalRegisteredDurationForThisRole

        if (currentActivity.projectRole.id == activityToUpdate.projectRole.id) {
            totalRegisteredDurationForThisRoleAfterDiscount =
                totalRegisteredDurationForThisRole - currentActivityDuration
        }
        return totalRegisteredDurationForThisRoleAfterDiscount + activityToUpdateDuration
    }

    private fun getRemaining(
        activityToUpdate: Activity,
        totalRegisteredDurationForThisRole: Int,
    ): Double {
        return (activityToUpdate.projectRole.maxAllowed - totalRegisteredDurationForThisRole.toDouble()) / DECIMAL_HOUR
    }

    private fun isExceedingMaxHoursForRole(
        currentActivity: Activity,
        activityToUpdate: Activity,
        year: Int,
        totalRegisteredDurationForThisRole: Int,
    ): Boolean {
        if (activityToUpdate.projectRole.maxAllowed > 0) {
            val totalRegisteredDurationForThisRoleAfterSave = getTotalRegisteredDurationForThisRoleAfterSave(
                currentActivity,
                activityToUpdate,
                year,
                totalRegisteredDurationForThisRole
            )
            return totalRegisteredDurationForThisRoleAfterSave > activityToUpdate.projectRole.maxAllowed
        }
        return false
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
        val projectToUpdate = projectService.findById(activityToUpdate.projectRole.project.id)
        val currentProject = projectService.findById(currentActivity.projectRole.project.id)
        val activityToUpdateStartYear = activityToUpdate.getYearOfStart()
        val activityToUpdateEndYear = activityToUpdate.timeInterval.end.year
        val totalRegisteredDurationForThisRoleStartYear =
            getTotalRegisteredDurationByProjectRole(activityToUpdate, activityToUpdateStartYear, user.id)
        val totalRegisteredDurationForThisRoleEndYear =
            getTotalRegisteredDurationByProjectRole(activityToUpdate, activityToUpdateEndYear, user.id)
        when {
            isProjectBlocked(
                projectToUpdate,
                activityToUpdate
            ) -> throw ProjectBlockedException(projectToUpdate.blockDate!!)

            isProjectBlocked(
                currentProject,
                currentActivity
            ) -> throw ProjectBlockedException(currentProject.blockDate!!)

            !activityToUpdate.projectRole.project.open -> throw ProjectClosedException()
            !isOpenPeriod(activityToUpdate.timeInterval.start) -> throw ActivityPeriodClosedException()
            isOverlappingAnotherActivityTime(activityToUpdate, user.id) -> throw OverlapsAnotherTimeException()
            user.isBeforeHiringDate(activityToUpdate.timeInterval.start.toLocalDate()) ->
                throw ActivityBeforeHiringDateException()

            activityToUpdate.isMoreThanOneDay() && activityToUpdate.timeUnit === TimeUnit.MINUTES -> throw ActivityPeriodNotValidException()

            isExceedingMaxHoursForRole(
                currentActivity,
                activityToUpdate,
                activityToUpdateStartYear,
                totalRegisteredDurationForThisRoleStartYear
            ) -> throw MaxHoursPerRoleException(
                activityToUpdate.projectRole.maxAllowed / DECIMAL_HOUR,
                getRemaining(
                    activityToUpdate,
                    totalRegisteredDurationForThisRoleStartYear
                ),
                activityToUpdateStartYear
            )

            (activityToUpdateStartYear != activityToUpdateEndYear) && isExceedingMaxHoursForRole(
                currentActivity,
                activityToUpdate,
                activityToUpdateEndYear,
                totalRegisteredDurationForThisRoleEndYear
            ) -> throw MaxHoursPerRoleException(
                activityToUpdate.projectRole.maxAllowed / DECIMAL_HOUR,
                getRemaining(
                    activityToUpdate,
                    totalRegisteredDurationForThisRoleEndYear
                ),
                activityToUpdateEndYear
            )
        }
    }

    @Transactional
    @ReadOnly
    fun checkActivityIsValidForDeletion(id: Long) {
        val activity = activityService.getActivityById(id)
        val project = projectService.findById(activity.projectRole.project.id)
        when {
            isProjectBlocked(project, activity) -> throw ProjectBlockedException(project.blockDate!!)
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

    @Transactional
    @ReadOnly
    fun checkActivityIsValidForApproval(id: Long) {
        val activity = activityService.getActivityById(id)
        when {
            activity.approvalState == ApprovalState.ACCEPTED || activity.approvalState == ApprovalState.NA -> throw InvalidActivityApprovalStateException()
            !activity.hasEvidences -> throw NoEvidenceInActivityException(id)
        }
    }

    private companion object {
        private const val DECIMAL_HOUR = 60.0
    }
}