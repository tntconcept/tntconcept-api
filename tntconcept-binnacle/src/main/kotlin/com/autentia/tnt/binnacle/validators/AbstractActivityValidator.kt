package com.autentia.tnt.binnacle.validators

import com.autentia.tnt.binnacle.core.domain.Activity
import com.autentia.tnt.binnacle.core.domain.Calendar
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityService
import io.archimedesfw.commons.time.ClockUtils
import java.time.LocalDateTime

internal abstract class AbstractActivityValidator(
    private val activityService: ActivityService,
    private val activityCalendarService: ActivityCalendarService,
) {
    protected fun isOpenPeriod(startDate: LocalDateTime): Boolean {
        return startDate.year >= ClockUtils.nowUtc().year - 1
    }

    protected fun isProjectOpen(project: Project): Boolean {
        return project.open
    }

    protected fun isProjectBlocked(project: Project, activity: Activity): Boolean {
        if (project.blockDate == null) {
            return false
        }
        return project.blockDate!!.isAfter(
            activity.getStart().toLocalDate()
        ) || project.blockDate!!.isEqual(activity.getStart().toLocalDate())
    }

    protected fun isBeforeProjectCreationDate(activity: Activity, project: Project): Boolean {
        return activity.timeInterval.start.toLocalDate() < project.startDate
    }
    protected fun isExceedingMaxTimeByActivity(activityToCreate: Activity): Boolean {
        if (activityToCreate.projectRole.timeInfo.maxTimeAllowed.byActivity == 0)
            return false

        val activityInterval = TimeInterval.of(activityToCreate.getStart(), activityToCreate.getEnd())
        val calendar = activityCalendarService.createCalendar(activityInterval.getDateInterval())

        val activityDuration = activityToCreate.getDuration(calendar)
        return activityDuration > activityToCreate.projectRole.timeInfo.maxTimeAllowed.byActivity
    }

    protected fun isEvidenceInputIncoherent(activity: Activity): Boolean {
        return activity.hasEvidences && activity.evidence == null
                || !activity.hasEvidences && activity.evidence != null
    }
    protected fun getTotalRegisteredDurationByProjectRole(
        activityToUpdate: Activity,
        year: Int,
        userId: Long,
    ): Int {
        val yearTimeInterval = TimeInterval.ofYear(year)

        val activities =
            activityService.getActivitiesByProjectRoleIds(
                yearTimeInterval,
                listOf(activityToUpdate.projectRole.id),
                userId
            ).filter { it.getYearOfStart() == yearTimeInterval.getYearOfStart() }

        val lastDateOfAllActivities =
            if (activities.isNotEmpty()) activities.maxOf { it.getEnd() } else yearTimeInterval.end

        val latestDateIsEqualOrHigher = lastDateOfAllActivities.year >= year

        val completeTimeInterval = TimeInterval.of(
            yearTimeInterval.start,
            if (latestDateIsEqualOrHigher) lastDateOfAllActivities else yearTimeInterval.end
        )

        val calendar = activityCalendarService.createCalendar(completeTimeInterval.getDateInterval())

        return activities.sumOf { it.getDuration(calendar) }
    }


    protected fun getTotalRegisteredDurationForThisRoleAfterSave(
        currentActivity: Activity,
        activityToUpdate: Activity,
        totalRegisteredDurationForThisRole: Int,
    ): Int {
        val activitiesCalendar = getActivitiesCalendar(currentActivity, activityToUpdate)

        var durationToReduce = 0
        if (currentActivity.projectRole.id == activityToUpdate.projectRole.id) {
            durationToReduce = currentActivity.getDuration(activitiesCalendar)
        }

        val activityToUpdateDuration = activityToUpdate.getDuration(activitiesCalendar)
        return totalRegisteredDurationForThisRole - durationToReduce + activityToUpdateDuration
    }

    protected fun getActivitiesCalendar(currentActivity: Activity, activityToUpdate: Activity): Calendar {
        val isCurrentActivityNotDefined = currentActivity.getYearOfStart() < 0
        val activitiesTimeInterval =
            if (isCurrentActivityNotDefined) {
                activityToUpdate.timeInterval
            } else {
                val activities = listOf(currentActivity, activityToUpdate)
                TimeInterval.of(
                    activities.minOf { it.getStart() },
                    activities.maxOf { it.getEnd() }
                )
            }

        return activityCalendarService.createCalendar(activitiesTimeInterval.getDateInterval())
    }


    protected fun getRemainingByTimeUnit(
        activityToUpdate: Activity,
        totalRegisteredDurationForThisRole: Int,
    ): Double {
        val remaining =
            (activityToUpdate.projectRole.getMaxTimeAllowedByYear() - totalRegisteredDurationForThisRole.toDouble())
        return when (activityToUpdate.timeUnit) {
            TimeUnit.DAYS -> remaining / (60 * 8)
            TimeUnit.NATURAL_DAYS -> remaining / (60 * 8)
            TimeUnit.MINUTES -> remaining
        }
    }

    protected fun isExceedingMaxTimeByRole(
        currentActivity: Activity,
        activityToUpdate: Activity,
        totalRegisteredDurationForThisRole: Int,
    ): Boolean {
        if (activityToUpdate.projectRole.getMaxTimeAllowedByYear() > 0) {
            val totalRegisteredDurationForThisRoleAfterSave = getTotalRegisteredDurationForThisRoleAfterSave(
                currentActivity,
                activityToUpdate,
                totalRegisteredDurationForThisRole
            )
            return totalRegisteredDurationForThisRoleAfterSave > activityToUpdate.projectRole.getMaxTimeAllowedByYear()
        }
        return false
    }

    protected fun ensureActivityCanBeDeleted(canAccessAllActivities: Boolean, activity: Activity) {
        if (!canAccessAllActivities) {
            require(activity.approvalState != ApprovalState.ACCEPTED) { "Cannot delete an activity already approved." }
        }
    }

    protected fun isOverlappingAnotherActivityTime(
        activity: Activity,
        userId: Long,
    ): Boolean {
        if (activity.duration == 0) {
            return false
        }
        val activities = activityService.findOverlappedActivities(activity.getStart(), activity.getEnd(), userId)
        return activities.size > 1 || activities.size == 1 && activities[0].id != activity.id
    }
}