package com.autentia.tnt.binnacle.validators

import com.autentia.tnt.binnacle.core.domain.ActivitiesRequestBody
import com.autentia.tnt.binnacle.core.domain.ActivityTimeOnly
import com.autentia.tnt.binnacle.core.utils.overlaps
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.exception.*
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import javax.transaction.Transactional

@Deprecated("Use ActivityValidator instead")
@Singleton
internal class ActivitiesValidator(
    private val activityRepository: ActivityRepository,
    private val projectRoleRepository: ProjectRoleRepository
) {
    @Transactional
    @ReadOnly
    fun checkActivityIsValidForCreation(activityRequest: ActivitiesRequestBody, user: User) {
        require(activityRequest.id == null) { "Cannot create a new activity with id ${activityRequest.id}." }

        val projectRoleDb = projectRoleRepository.findById(activityRequest.projectRoleId)
        when {
            projectRoleDb === null -> throw ProjectRoleNotFoundException(activityRequest.projectRoleId)
            !isProjectOpen(projectRoleDb.project) -> throw ProjectClosedException()
            !isOpenPeriod(activityRequest.startDate) -> throw ActivityPeriodClosedException()
            isOverlappingAnotherActivityTime(activityRequest, user) -> throw OverlapsAnotherTimeException()
            isBeforeHiringDate(
                activityRequest.startDate.toLocalDate(),
                user
            ) -> throw ActivityBeforeHiringDateException()

        }
        checkIfIsExceedingMaxHoursForRole(Activity.emptyActivity(projectRoleDb!!), activityRequest, projectRoleDb, user)
    }

    private fun checkIfIsExceedingMaxHoursForRole(
        currentActivity: Activity,
        activityRequest: ActivitiesRequestBody,
        projectRole: ProjectRole,
        user: User
    ) {
        if (projectRole.maxAllowed > 0) {
            val year = activityRequest.startDate.year
            val activitiesSinceStartOfYear = getActivitiesInYear(year, user)
            val totalRegisteredHoursForThisRole =
                getTotalHoursPerRole(activitiesSinceStartOfYear, activityRequest)
            var totalRegisteredHoursForThisRoleAfterDiscount = totalRegisteredHoursForThisRole

            if (currentActivity.projectRole.id == activityRequest.projectRoleId) {
                totalRegisteredHoursForThisRoleAfterDiscount =
                    totalRegisteredHoursForThisRole - currentActivity.duration
            }

            val totalRegisteredHoursAfterSaveRequested =
                totalRegisteredHoursForThisRoleAfterDiscount + activityRequest.duration


            if (totalRegisteredHoursAfterSaveRequested > projectRole.maxAllowed) {
                val remainingTime = (projectRole.maxAllowed - totalRegisteredHoursForThisRole.toDouble()) / DECIMAL_HOUR

                throw MaxHoursPerRoleException(
                    projectRole.maxAllowed / DECIMAL_HOUR,
                    remainingTime,
                    year
                )
            }
        }
    }

    private fun isBeforeHiringDate(startDate: LocalDate, user: User): Boolean {
        return startDate.isBefore(user.hiringDate)
    }

    private fun isOpenPeriod(startDate: LocalDateTime): Boolean {
        return startDate.year >= LocalDateTime.now().year - 1
    }

    private fun getTotalHoursPerRole(
        activitiesInYear: List<ActivityTimeOnly>,
        activityRequest: ActivitiesRequestBody
    ) =
        activitiesInYear
            .filter { it.projectRoleId == activityRequest.projectRoleId }
            .sumOf { it.duration }

    private fun getActivitiesInYear(year: Int, user: User) =
        activityRepository.findWorkedMinutes(
            LocalDateTime.of(year, Month.JANUARY, 1, 0, 0),
            LocalDateTime.of(year, Month.DECEMBER, 31, 23, 59),
        )

    @Transactional
    @ReadOnly
    fun checkActivityIsValidForUpdate(activityRequest: ActivitiesRequestBody, user: User) {
        require(activityRequest.id != null) { "Cannot update an activity without id." }

        val activityDb = activityRepository.findById(activityRequest.id)
        val projectRoleDb = projectRoleRepository.findById(activityRequest.projectRoleId)
        when {
            activityDb == null -> throw ActivityNotFoundException(activityRequest.id!!)
            projectRoleDb === null -> throw ProjectRoleNotFoundException(activityRequest.projectRoleId)
            !userHasAccess(activityDb, user) -> throw UserPermissionException()
            !isProjectOpen(projectRoleDb.project) -> throw ProjectClosedException()
            !isOpenPeriod(activityRequest.startDate) -> throw ActivityPeriodClosedException()
            isOverlappingAnotherActivityTime(activityRequest, user) -> throw OverlapsAnotherTimeException()
            isBeforeHiringDate(
                activityRequest.startDate.toLocalDate(),
                user
            ) -> throw ActivityBeforeHiringDateException()
        }
        checkIfIsExceedingMaxHoursForRole(activityDb!!, activityRequest, projectRoleDb!!, user)
    }


    @Transactional
    @ReadOnly
    fun checkActivityIsValidForDeletion(id: Long, user: User) {
        val activityDb = activityRepository.findById(id)
        when {
            activityDb === null -> throw ActivityNotFoundException(id)
            !isOpenPeriod(activityDb.start) -> throw ActivityPeriodClosedException()
            !userHasAccess(activityDb, user) -> throw UserPermissionException()
        }
    }

    fun userHasAccess(activityDb: Activity, user: User): Boolean {
        return activityDb.userId == user.id
    }

    fun isProjectOpen(project: Project): Boolean {
        return project.open
    }

    private fun isOverlappingAnotherActivityTime(activityRequest: ActivitiesRequestBody, user: User): Boolean {
        if (activityRequest.duration == 0) {
            return false
        }

        val startDate = activityRequest.startDate.withHour(0).withMinute(0).withSecond(0)
        val endDate = activityRequest.startDate.withHour(23).withMinute(59).withSecond(59)
        val activities = activityRepository.find(startDate, endDate)

        return checkTimeOverlapping(activityRequest, activities)
    }

    private fun checkTimeOverlapping(activityRequest: ActivitiesRequestBody, activities: List<Activity>): Boolean {
        val startDate = activityRequest.startDate
        val endDate = activityRequest.startDate.plusMinutes(activityRequest.duration.toLong())

        return activities.any {
            val otherStartDate = it.start
            val otherEndDate = it.start.plusMinutes(it.duration.toLong())
            activityRequest.id != it.id && it.duration > 0 && (startDate..endDate).overlaps(otherStartDate..otherEndDate)
        }
    }

    private companion object {
        private const val DECIMAL_HOUR = 60.0
    }
}