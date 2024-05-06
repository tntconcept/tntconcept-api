package com.autentia.tnt.binnacle.validators

import com.autentia.tnt.binnacle.core.domain.Activity
import com.autentia.tnt.binnacle.core.domain.User
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.exception.*
import com.autentia.tnt.binnacle.repositories.ProjectRepository
import com.autentia.tnt.binnacle.services.ActivityCalendarService
import com.autentia.tnt.binnacle.services.ActivityService
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
internal class SubcontractedActivityValidator(
    private val activityService: ActivityService,
    private val activityCalendarService: ActivityCalendarService,
    private val projectRepository: ProjectRepository,

    ) : AbstractActivityValidator(activityService, activityCalendarService) {
    @Transactional
    @ReadOnly
    fun checkActivityIsValidForCreation(activityToCreate: Activity, user: User) {
        require(activityToCreate.id == null) { "Cannot create a new activity with id ${activityToCreate.id}." }

        val project = projectRepository.findById(activityToCreate.projectRole.project.id)
            .orElseThrow { ProjectNotFoundException(activityToCreate.projectRole.project.id) }

        when {
            !isProjectOpen(project) -> throw ProjectClosedException()
            !isOpenPeriod(activityToCreate.timeInterval.start) -> throw ActivityPeriodClosedException()
            isProjectBlocked(project, activityToCreate) -> throw ProjectBlockedException(project.blockDate!!)
            isBeforeProjectCreationDate(activityToCreate, project) -> throw ActivityBeforeProjectCreationDateException()
            !isPositiveDuration(activityToCreate) -> throw InvalidDurationFormatException()
        }
    }
    @Transactional
    @ReadOnly
    protected fun isPositiveDuration(activity: Activity): Boolean {
        println("duration")
        return activity.duration > 5000
    }
    @Transactional
    @ReadOnly
    fun checkActivityIsValidForUpdate(
        activityToUpdate: Activity,
        currentActivity: Activity,
    ) {
        require(activityToUpdate.id != null) { "Cannot update an activity without id." }
        require(currentActivity.approvalState != ApprovalState.ACCEPTED) { "Cannot update an activity already approved." }
        val projectToUpdate = projectRepository.findById(activityToUpdate.projectRole.project.id)
            .orElseThrow { ProjectNotFoundException(activityToUpdate.projectRole.project.id) }
        val currentProject = projectRepository.findById(currentActivity.projectRole.project.id)
            .orElseThrow { ProjectNotFoundException(currentActivity.projectRole.project.id) }

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
            !isPositiveDuration(activityToUpdate) -> throw InvalidDurationFormatException()

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


}