package com.autentia.tnt.binnacle.validators

import com.autentia.tnt.binnacle.core.domain.Activity
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.exception.ActivityBeforeHiringDateException
import com.autentia.tnt.binnacle.exception.ActivityNotFoundException
import com.autentia.tnt.binnacle.exception.ActivityPeriodClosedException
import com.autentia.tnt.binnacle.exception.ActivityPeriodNotValidException
import com.autentia.tnt.binnacle.exception.MaxHoursPerRoleException
import com.autentia.tnt.binnacle.exception.OverlapsAnotherTimeException
import com.autentia.tnt.binnacle.exception.ProjectClosedException
import com.autentia.tnt.binnacle.exception.ProjectRoleNotFoundException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityService
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import java.time.LocalDateTime
import javax.transaction.Transactional

@Singleton
internal class ActivityValidator(
    private val activityService: ActivityService,
    private val activityRepository: ActivityRepository,
    private val activityCalendarService: ActivityCalendarService,
    private val projectRoleRepository: ProjectRoleRepository
) {
    @Transactional
    @ReadOnly
    fun checkActivityIsValidForCreation(activityToCreate: Activity, user: com.autentia.tnt.binnacle.core.domain.User) {
        require(activityToCreate.id == null) { "Cannot create a new activity with id ${activityToCreate.id}." }

        val projectRoleDb = projectRoleRepository.findById(activityToCreate.projectRole.id)
        when {
            projectRoleDb == null -> throw ProjectRoleNotFoundException(activityToCreate.projectRole.id)
            !isProjectOpen(projectRoleDb.project) -> throw ProjectClosedException()
            !isOpenPeriod(activityToCreate.timeInterval.start) -> throw ActivityPeriodClosedException()
            isOverlappingAnotherActivityTime(activityToCreate) -> throw OverlapsAnotherTimeException()
            user.isBeforeHiringDate(activityToCreate.timeInterval.start.toLocalDate()) ->
                throw ActivityBeforeHiringDateException()

            activityToCreate.isMoreThanOneDay() && activityToCreate.timeUnit === TimeUnit.MINUTES -> throw ActivityPeriodNotValidException()
        }
        checkIfIsExceedingMaxHoursForRole(Activity.emptyActivity(activityToCreate.projectRole, user), activityToCreate)
    }

    private fun checkIfIsExceedingMaxHoursForRole(currentActivity: Activity, activityToUpdate: Activity) {
        checkIfIsExceedingMaxHoursForRole(
            currentActivity, activityToUpdate, activityToUpdate.getYearOfStart()
        )
        if (activityToUpdate.getYearOfStart() != activityToUpdate.getYearOfEnd()) {
            checkIfIsExceedingMaxHoursForRole(
                currentActivity, activityToUpdate, activityToUpdate.timeInterval.end.year
            )
        }
    }

    private fun checkIfIsExceedingMaxHoursForRole(
        currentActivity: Activity,
        activityToUpdate: Activity,
        year: Int
    ) {
        if (activityToUpdate.projectRole.maxAllowed > 0) {

            val yearTimeInterval = TimeInterval.ofYear(year)

            val yearCalendar = activityCalendarService.createCalendar(yearTimeInterval.getDateInterval())

            val currentActivityDuration = currentActivity.getDurationByCountingWorkableDays(yearCalendar)
            val activityToUpdateDuration = activityToUpdate.getDurationByCountingWorkableDays(yearCalendar)
            val activities =
                activityService.getActivitiesByProjectRoleIds(yearTimeInterval, listOf(activityToUpdate.projectRole.id))
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
        user: com.autentia.tnt.binnacle.core.domain.User
    ) {
        require(activityToUpdate.id != null) { "Cannot update an activity without id." }
        val activityDb = activityRepository.findById(activityToUpdate.id)
        val projectRoleDb = projectRoleRepository.findById(activityToUpdate.projectRole.id)
        when {
            activityDb === null -> throw ActivityNotFoundException(activityToUpdate.id)
            projectRoleDb === null -> throw ProjectRoleNotFoundException(activityToUpdate.projectRole.id)
            !activityToUpdate.projectRole.project.open -> throw ProjectClosedException()
            !isOpenPeriod(activityToUpdate.timeInterval.start) -> throw ActivityPeriodClosedException()
            isOverlappingAnotherActivityTime(activityToUpdate) -> throw OverlapsAnotherTimeException()
            user.isBeforeHiringDate(activityToUpdate.timeInterval.start.toLocalDate()) ->
                throw ActivityBeforeHiringDateException()

            activityToUpdate.isMoreThanOneDay() && activityToUpdate.timeUnit === TimeUnit.MINUTES -> throw ActivityPeriodNotValidException()
        }
        checkIfIsExceedingMaxHoursForRole(currentActivity, activityToUpdate)
    }


    @Transactional
    @ReadOnly
    fun checkActivityIsValidForDeletion(id: Long) {
        val activityDb = activityRepository.findById(id)
        when {
            activityDb === null -> throw ActivityNotFoundException(id)
            !isOpenPeriod(activityDb.start) -> throw ActivityPeriodClosedException()
        }
    }

    fun userHasAccess(activityDb: Activity, user: User): Boolean {
        return activityDb.userId == user.id
    }

    fun isProjectOpen(project: Project): Boolean {
        return project.open
    }

    private fun isOverlappingAnotherActivityTime(
        activity: Activity
    ): Boolean {
        if (activity.duration == 0) {
            return false
        }
        val activities = activityRepository.findOverlapped(activity.getStart(), activity.getEnd())
        return activities.size > 1 || activities.size == 1 && activities[0].id != activity.id
    }

    private companion object {
        private const val DECIMAL_HOUR = 60.0
    }
}