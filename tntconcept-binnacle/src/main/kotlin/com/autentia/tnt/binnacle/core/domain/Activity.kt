package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.TimeUnit
import java.time.LocalDateTime
import java.time.LocalTime

data class Activity private constructor(
    val id: Long? = null,
    override val timeInterval: TimeInterval,
    val duration: Int,
    val description: String,
    val projectRole: ProjectRole,
    val userId: Long,
    val billable: Boolean,
    val departmentId: Long?,
    var insertDate: LocalDateTime? = null,
    val hasEvidences: Boolean,
    var approvalState: ApprovalState
) : ActivityTimeInterval(timeInterval, projectRole.timeUnit) {
    fun getStart() = timeInterval.start
    fun getEnd() = timeInterval.end
    fun getYearOfStart() = timeInterval.getYearOfStart()
    fun getYearOfEnd() = timeInterval.getYearOfEnd()
    fun getDurationInUnits(): Int {
        if (timeUnit === TimeUnit.DAYS) {
            return duration / (60 * 8)
        }
        return duration
    }

    fun isMoreThanOneDay() = projectRole.timeUnit === TimeUnit.DAYS || timeInterval.getDuration().toDays().toInt() > 0

    fun activityCanBeApproved() =
        RequireEvidence.isRequired(projectRole.requireEvidence) && approvalState === ApprovalState.PENDING && hasEvidences

    fun isWorkingTimeActivity() = projectRole.isWorkingTime

    companion object {

        fun of(
            id: Long?,
            timeInterval: TimeInterval,
            duration: Int,
            description: String,
            projectRole: ProjectRole,
            userId: Long,
            billable: Boolean,
            departmentId: Long?,
            insertDate: LocalDateTime?,
            hasEvidences: Boolean,
            approvalState: ApprovalState,
        ) = Activity(
            id,
            TimeInterval.of(
                getDateAtTimeIfNecessary(timeInterval.start, projectRole.timeUnit, LocalTime.MIN),
                getDateAtTimeIfNecessary(timeInterval.end, projectRole.timeUnit, LocalTime.of(23, 59, 59))
            ),
            duration,
            description,
            projectRole,
            userId,
            billable,
            departmentId,
            insertDate,
            hasEvidences,
            approvalState
        )

        fun emptyActivity(projectRole: ProjectRole, user: User) = Activity(
            0,
            TimeInterval.of(LocalDateTime.MIN, LocalDateTime.MIN),
            0,
            "Empty activity",
            projectRole,
            user.id,
            false,
            null,
            LocalDateTime.MIN,
            false,
            ApprovalState.NA,
        )
    }
}