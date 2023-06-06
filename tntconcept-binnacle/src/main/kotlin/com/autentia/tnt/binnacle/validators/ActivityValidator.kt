package com.autentia.tnt.binnacle.validators

import com.autentia.tnt.binnacle.core.domain.Activity
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.exception.*
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.services.ProjectService
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import java.time.LocalDate
import java.time.LocalDateTime
import javax.transaction.Transactional

@Singleton
internal class ActivityValidator(
    private val activityService: ActivityService,
    private val activityRepository: ActivityRepository,
    private val activityCalendarService: ActivityCalendarService,
    private val projectRoleRepository: ProjectRoleRepository,
    private val projectService: ProjectService,
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
        user: com.autentia.tnt.binnacle.core.domain.User,
    ) {
        require(activityToUpdate.id != null) { "Cannot update an activity without id." }
        val activityDb = activityRepository.findById(activityToUpdate.id)
        val projectRoleDb = projectRoleRepository.findById(activityToUpdate.projectRole.id)
        val projectToUpdate = projectService.getProjectById(activityToUpdate.projectRole.project.id)
        val currentProject = projectService.getProjectById(currentActivity.projectRole.project.id)
        val today = LocalDate.now()
        val activityToUpdateDate = activityToUpdate.getStart().toLocalDate()
        when {
            activityDb === null -> throw ActivityNotFoundException(activityToUpdate.id)
            projectRoleDb === null -> throw ProjectRoleNotFoundException(activityToUpdate.projectRole.id)
            isProjectBlocked(projectToUpdate, activityToUpdateDate) -> throw ProjectBlockedException()
            isProjectBlocked(currentProject, today) -> throw ProjectBlockedException()
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
        val activityDb = activityRepository.findById(id)
        when {
            activityDb === null -> throw ActivityNotFoundException(id)
            !isOpenPeriod(activityDb.start) -> throw ActivityPeriodClosedException()
        }
    }

    fun isProjectOpen(project: Project): Boolean {
        return project.open
    }

    fun isProjectBlocked(project: com.autentia.tnt.binnacle.core.domain.Project, date: LocalDate): Boolean {
        return if (project.blockDate != null) {
            date.isBefore(project.blockDate)
        } else {
            false
        }
    }

    private fun isOverlappingAnotherActivityTime(
        activity: Activity,
        userId: Long,
    ): Boolean {
        if (activity.duration == 0) {
            return false
        }
        val activities = activityRepository.findOverlapped(activity.getStart(), activity.getEnd(), userId)
        return activities.size > 1 || activities.size == 1 && activities[0].id != activity.id
    }

    private companion object {
        private const val DECIMAL_HOUR = 60.0
    }
}