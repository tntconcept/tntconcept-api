package com.autentia.tnt.binnacle.validators

import com.autentia.tnt.binnacle.core.domain.Activity
import com.autentia.tnt.binnacle.core.domain.Calendar
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.core.domain.User
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.exception.*
import com.autentia.tnt.binnacle.repositories.ProjectRepository
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityService
import io.archimedesfw.commons.time.ClockUtils
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import java.time.LocalDateTime
import javax.transaction.Transactional

@Singleton
internal class SubcontractedActivityValidator(
        private val activityService: ActivityService,
        private val activityCalendarService: ActivityCalendarService,
        private val projectRepository: ProjectRepository,

) {
    @Transactional
    @ReadOnly
    fun checkActivityIsValidForCreation(activityToCreate: Activity, user: User) {
        require(activityToCreate.id == null) { "Cannot create a new activity with id ${activityToCreate.id}." }

        val project = projectRepository.findById(activityToCreate.projectRole.project.id)
                .orElseThrow { ProjectNotFoundException(activityToCreate.projectRole.project.id) }

        when {
            isEvidenceInputIncoherent(activityToCreate) -> throw NoEvidenceInActivityException("Activity sets hasEvidence to true but no evidence was found")
            !isProjectOpen(project) -> throw ProjectClosedException()
            !isOpenPeriod(activityToCreate.timeInterval.start) -> throw ActivityPeriodClosedException()
            isProjectBlocked(project, activityToCreate) -> throw ProjectBlockedException(project.blockDate!!)
            isBeforeProjectCreationDate(activityToCreate, project) -> throw ActivityBeforeProjectCreationDateException()
            isOverlappingAnotherActivityTime(activityToCreate, user.id) -> throw OverlapsAnotherTimeException()

        }
    }


    private fun isEvidenceInputIncoherent(activity: Activity): Boolean {
        return activity.hasEvidences && activity.evidence == null
                || !activity.hasEvidences && activity.evidence != null
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


        }
    }



    @Transactional
    @ReadOnly
    fun checkActivityIsValidForDeletion(activity: Activity) {
        val project = projectRepository.findById(activity.projectRole.project.id)
                .orElseThrow { ProjectNotFoundException(activity.projectRole.project.id) }

        when {
            !isProjectOpen(project) -> throw ProjectClosedException()
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
        return project.blockDate!!.isAfter(
                activity.getStart().toLocalDate()
        ) || project.blockDate!!.isEqual(activity.getStart().toLocalDate())
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


    fun isBeforeProjectCreationDate(activity: Activity, project: Project): Boolean {
        return activity.timeInterval.start.toLocalDate() < project.startDate
    }
}