package com.autentia.tnt.binnacle.validators

import com.autentia.tnt.binnacle.core.domain.ActivityRequestBody
import com.autentia.tnt.binnacle.core.domain.ActivityTimeOnly
import com.autentia.tnt.binnacle.core.utils.overlaps
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
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import javax.transaction.Transactional
import kotlin.math.absoluteValue

@Singleton
internal class ActivityValidator(
    private val activityRepository: ActivityRepository,
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
            !isOpenPeriod(activityRequest.startDate) -> throw ActivityPeriodClosedException()
            isOverlappingAnotherActivityTime(activityRequest, user) -> throw OverlapsAnotherTimeException()
            isBeforeHiringDate(
                activityRequest.startDate.toLocalDate(),
                user
            ) -> throw ActivityBeforeHiringDateException()

            isExceedingMaxHoursForRole(activityRequest, projectRoleDb, user) -> throw MaxHoursPerRoleException(
                projectRoleDb.maxAllowed / DECIMAL_HOUR,
                getRemainingTime(projectRoleDb, activityRequest, user) / DECIMAL_HOUR
            )
        }
    }

    private fun getRemainingTime(projectRole: ProjectRole, activityRequest: ActivityRequestBody, user: User): Double {
        val activitiesInYear = getActivitiesInYear(LocalDate.now().year, user)
        val pastRegisteredTime = getTotalHoursPerRoleAndActivity(activitiesInYear, activityRequest)
        var remainingTime = 0.0
        if (pastRegisteredTime < projectRole.maxAllowed) {
            remainingTime = ((projectRole.maxAllowed - pastRegisteredTime).toDouble())
        }
        return remainingTime
    }

    private fun isBeforeHiringDate(startDate: LocalDate, user: User): Boolean {
        return startDate.isBefore(user.hiringDate)
    }

    private fun isOpenPeriod(startDate: LocalDateTime): Boolean {
        return startDate.year >= LocalDateTime.now().year - 1
    }

    private fun getTotalHoursPerRoleAndActivity(
        activitiesInYear: List<ActivityTimeOnly>,
        activityRequest: ActivityRequestBody
    ) =
        activitiesInYear
            .filter { it.projectRoleId == activityRequest.projectRoleId }
            .sumOf { it.duration }

    private fun getActivitiesInYear(year: Int, user: User) =
        activityRepository.workedMinutesBetweenDate(
            LocalDateTime.of(year, Month.JANUARY, 1, 0, 0),
            LocalDateTime.of(year, Month.DECEMBER, 31, 23, 59),
            user.id
        )

    private fun isExceedingMaxHoursForRole(
        activityRequest: ActivityRequestBody,
        projectRole: ProjectRole,
        user: User
    ): Boolean {
        var isExceedingMaxHours = false
        if (projectRole.maxAllowed > 0) {
            val activitiesSinceStartOfYear = getActivitiesInYear(currentYear, user)

            val totalRegisteredHoursForThisRole =
                getTotalHoursPerRoleAndActivity(activitiesSinceStartOfYear, activityRequest)

            isExceedingMaxHours = (totalRegisteredHoursForThisRole + activityRequest.duration) > projectRole.maxAllowed
        }
        return isExceedingMaxHours
    }

    private fun isExceedingMaxHoursForRoleUpdate(
        currentActivity: Activity,
        activityRequestUpdate: ActivityRequestBody,
        projectRole: ProjectRole,
        user: User
    ): Boolean {
        var isExceedingMaxHours = false
        if (projectRole.maxAllowed > 0) {
            val activitiesSinceStartOfYear = getActivitiesInYear(currentYear, user)

            val totalRegisteredHoursForThisRole =
                getTotalHoursPerRoleAndActivity(activitiesSinceStartOfYear, activityRequestUpdate)

            if (currentActivity.duration < activityRequestUpdate.duration) {
                isExceedingMaxHours =
                    (totalRegisteredHoursForThisRole + ((currentActivity.duration - activityRequestUpdate.duration).absoluteValue) > projectRole.maxAllowed)
            }

        }
        return isExceedingMaxHours
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
            !isOpenPeriod(activityRequest.startDate) -> throw ActivityPeriodClosedException()
            isOverlappingAnotherActivityTime(activityRequest, user) -> throw OverlapsAnotherTimeException()
            isBeforeHiringDate(
                activityRequest.startDate.toLocalDate(),
                user
            ) -> throw ActivityBeforeHiringDateException()

            isExceedingMaxHoursForRoleUpdate(
                activityDb,
                activityRequest,
                projectRoleDb,
                user
            ) -> throw MaxHoursPerRoleException(
                projectRoleDb.maxAllowed / DECIMAL_HOUR,
                getRemainingTime(projectRoleDb, activityRequest, user) / DECIMAL_HOUR
            )
        }
    }


    @Transactional
    @ReadOnly
    fun checkActivityIsValidForDeletion(id: Long, user: User) {
        val activityDb = activityRepository.findById(id).orElse(null)
        when {
            activityDb === null -> throw ActivityNotFoundException(id)
            !isOpenPeriod(activityDb.startDate) -> throw ActivityPeriodClosedException()
            !userHasAccess(activityDb, user) -> throw UserPermissionException()
        }
    }

    fun userHasAccess(activityDb: Activity, user: User): Boolean {
        return activityDb.userId == user.id
    }

    fun isProjectOpen(project: Project): Boolean {
        return project.open
    }

    private fun isOverlappingAnotherActivityTime(activityRequest: ActivityRequestBody, user: User): Boolean {
        if (activityRequest.duration == 0) {
            return false
        }

        val startDate = activityRequest.startDate.withHour(0).withMinute(0).withSecond(0)
        val endDate = activityRequest.startDate.withHour(23).withMinute(59).withSecond(59)
        val activities = activityRepository.getActivitiesBetweenDate(startDate, endDate, user.id)

        return checkTimeOverlapping(activityRequest, activities)
    }

    private fun checkTimeOverlapping(activityRequest: ActivityRequestBody, activities: List<Activity>): Boolean {
        val startDate = activityRequest.startDate
        val endDate = activityRequest.startDate.plusMinutes(activityRequest.duration.toLong())

        return activities.any {
            val otherStartDate = it.startDate
            val otherEndDate = it.startDate.plusMinutes(it.duration.toLong())
            activityRequest.id != it.id && it.duration > 0 && (startDate..endDate).overlaps(otherStartDate..otherEndDate)
        }
    }

    private companion object {
        private const val DECIMAL_HOUR = 60.0
        private val currentYear = LocalDate.now().year
    }
}
