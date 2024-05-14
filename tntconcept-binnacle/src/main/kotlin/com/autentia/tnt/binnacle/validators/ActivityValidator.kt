package com.autentia.tnt.binnacle.validators

import com.autentia.tnt.binnacle.core.domain.Activity
import com.autentia.tnt.binnacle.core.domain.User
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.Billable
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.exception.*
import com.autentia.tnt.binnacle.repositories.ProjectRepository
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityService
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
internal class ActivityValidator(
    private val activityService: ActivityService,
    private val activityCalendarService: ActivityCalendarService,
    private val projectRepository: ProjectRepository,
):AbstractActivityValidator(activityService,activityCalendarService) {
    @Transactional
    @ReadOnly
    fun checkActivityIsValidForCreation(activityToCreate: Activity, user: User) {
        require(activityToCreate.id == null) { "Cannot create a new activity with id ${activityToCreate.id}." }
        val project = projectRepository.findById(activityToCreate.projectRole.project.id)
            .orElseThrow { ProjectNotFoundException(activityToCreate.projectRole.project.id) }
        val emptyActivity = Activity.emptyActivity(activityToCreate.projectRole, user)
        val activityToCreateStartYear = activityToCreate.getYearOfStart()
        val totalRegisteredDurationForThisRoleStartYear =
            getTotalRegisteredDurationByProjectRole(emptyActivity, activityToCreateStartYear, user.id)

        when {
            isEvidenceInputIncoherent(activityToCreate) -> throw NoEvidenceInActivityException("Activity sets hasEvidence to true but no evidence was found")
            !isProjectOpen(project) -> throw ProjectClosedException()
            !isOpenPeriod(activityToCreate.timeInterval.start) -> throw ActivityPeriodClosedException()
            isProjectBlocked(project, activityToCreate) -> throw ProjectBlockedException(project.blockDate!!)
            isBeforeProjectCreationDate(activityToCreate, project) -> throw ActivityBeforeProjectCreationDateException()
            isOverlappingAnotherActivityTime(activityToCreate, user.id) -> throw OverlapsAnotherTimeException()
            !isActivityBillableCoherenceWithProjectBillingType(activityToCreate) -> throw ActivityBillableIncoherenceException()
            user.isBeforeHiringDate(activityToCreate.timeInterval.start.toLocalDate()) ->
                throw ActivityBeforeHiringDateException()

            activityToCreate.isMoreThanOneDay() && activityToCreate.timeUnit === TimeUnit.MINUTES -> throw ActivityPeriodNotValidException()
            isExceedingMaxTimeByActivity(activityToCreate) -> throw MaxTimePerActivityRoleException(
                activityToCreate.projectRole.getMaxTimeAllowedByActivityInTimeUnits(),
                activityToCreate.projectRole.getMaxTimeAllowedByActivityInTimeUnits(),
                activityToCreate.projectRole.timeInfo.timeUnit,
                activityToCreateStartYear
            )

            isExceedingMaxTimeByRole(
                emptyActivity,
                activityToCreate,
                totalRegisteredDurationForThisRoleStartYear
            ) -> throw MaxTimePerRoleException(
                activityToCreate.projectRole.getMaxTimeAllowedByYearInTimeUnits(),
                getRemainingByTimeUnit(
                    activityToCreate,
                    totalRegisteredDurationForThisRoleStartYear
                ),
                activityToCreate.timeUnit,
                activityToCreateStartYear
            )
        }
    }

    private fun isActivityBillableCoherenceWithProjectBillingType(activity: Activity):Boolean{
        when(activity.projectRole.project.projectBillingType.type){
            Billable.NEVER -> return !activity.billable
            Billable.ALWAYS -> return activity.billable
            Billable.OPTIONAL -> return true
        }
    }
    private fun isExceedingMaxTimeByActivity(activityToCreate: Activity): Boolean {
        if (activityToCreate.projectRole.timeInfo.maxTimeAllowed.byActivity == 0)
            return false

        val activityInterval = TimeInterval.of(activityToCreate.getStart(), activityToCreate.getEnd())
        val calendar = activityCalendarService.createCalendar(activityInterval.getDateInterval())

        val activityDuration = activityToCreate.getDuration(calendar)
        return activityDuration > activityToCreate.projectRole.timeInfo.maxTimeAllowed.byActivity
    }

    private fun isEvidenceInputIncoherent(activity: Activity): Boolean {
        return activity.hasEvidences && activity.evidence == null
                || !activity.hasEvidences && activity.evidence != null
    }

    private fun getTotalRegisteredDurationByProjectRole(
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


    private fun getTotalRegisteredDurationForThisRoleAfterSave(
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

    private fun getActivitiesCalendar(currentActivity: Activity, activityToUpdate: Activity): Calendar {
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


    private fun getRemainingByTimeUnit(
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

    private fun isExceedingMaxTimeByRole(
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

    private fun isOpenPeriod(startDate: LocalDateTime): Boolean {
        return startDate.year >= ClockUtils.nowUtc().year - 1
    }

    @Transactional
    @ReadOnly
    fun checkActivityIsValidForUpdate(
        activityToUpdate: Activity,
        currentActivity: Activity,
        user: User,
    ) {
        require(activityToUpdate.id != null) { "Cannot update an activity without id." }
        require(currentActivity.approvalState != ApprovalState.ACCEPTED) { "Cannot update an activity already approved." }
        val projectToUpdate = projectRepository.findById(activityToUpdate.projectRole.project.id)
            .orElseThrow { ProjectNotFoundException(activityToUpdate.projectRole.project.id) }
        val currentProject = projectRepository.findById(currentActivity.projectRole.project.id)
            .orElseThrow { ProjectNotFoundException(currentActivity.projectRole.project.id) }
        val activityToUpdateStartYear = activityToUpdate.getYearOfStart()
        val totalRegisteredDurationForThisRoleStartYear =
            getTotalRegisteredDurationByProjectRole(activityToUpdate, activityToUpdateStartYear, user.id)
        when {
            isEvidenceInputIncoherent(activityToUpdate) -> throw NoEvidenceInActivityException("Activity sets hasEvidence to true but no evidence was found")

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
            !isActivityBillableCoherenceWithProjectBillingType(activityToUpdate) -> throw ActivityBillableIncoherenceException()
            user.isBeforeHiringDate(activityToUpdate.timeInterval.start.toLocalDate()) ->
                throw ActivityBeforeHiringDateException()

            activityToUpdate.isMoreThanOneDay() && activityToUpdate.timeUnit === TimeUnit.MINUTES -> throw ActivityPeriodNotValidException()

            isExceedingMaxTimeByActivity(activityToUpdate) -> throw MaxTimePerActivityRoleException(
                activityToUpdate.projectRole.timeInfo.getMaxTimeAllowedByActivityInUnits(),
                activityToUpdate.projectRole.timeInfo.getMaxTimeAllowedByActivityInUnits(),
                activityToUpdate.projectRole.timeInfo.timeUnit,
                activityToUpdateStartYear,
            )

            isExceedingMaxTimeByRole(
                currentActivity,
                activityToUpdate,
                totalRegisteredDurationForThisRoleStartYear
            ) -> throw MaxTimePerRoleException(
                activityToUpdate.projectRole.getMaxTimeAllowedByYearInTimeUnits(),
                getRemainingByTimeUnit(
                    activityToUpdate,
                    totalRegisteredDurationForThisRoleStartYear
                ),
                activityToUpdate.timeUnit,
                activityToUpdateStartYear
            )
        }
    }

    @Transactional
    @ReadOnly
    fun checkActivityIsValidForDeletion(activity: Activity) {
        internalCheckActivityIsValidForDeletion(activity, false)
    }

    @Transactional
    @ReadOnly
    fun checkAllAccessActivityIsValidForDeletion(activity: Activity) {
        internalCheckActivityIsValidForDeletion(activity, true)
    }

    private fun internalCheckActivityIsValidForDeletion(activity: Activity, canAccessAllActivities: Boolean = false) {
        val project = projectRepository.findById(activity.projectRole.project.id)
            .orElseThrow { ProjectNotFoundException(activity.projectRole.project.id) }


        ensureActivityCanBeDeleted(canAccessAllActivities, activity)

        when {
            !isProjectOpen(project) -> throw ProjectClosedException()
            isProjectBlocked(project, activity) -> throw ProjectBlockedException(project.blockDate!!)
            !isOpenPeriod(activity.getStart()) -> throw ActivityPeriodClosedException()
        }
    }





}
