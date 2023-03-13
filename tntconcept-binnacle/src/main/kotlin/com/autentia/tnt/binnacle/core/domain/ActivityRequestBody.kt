package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.entities.TimeUnit
import java.time.LocalDateTime
import javax.validation.constraints.Size

data class ActivityRequestBody(

    val id: Long? = null,
    val interval: Interval,

    @field:Size(max = 2048, message = "Description must not exceed 2048 characters")
    val description: String,

    val billable: Boolean,
    val projectRoleId: Long,
    val timeUnit: TimeUnit,
    val hasEvidences: Boolean,
    val imageFile: String? = null
) {
    constructor(
        id: Long? = null, start: LocalDateTime, end: LocalDateTime, description: String,
        billable: Boolean, projectRoleId: Long, timeUnit: TimeUnit, hasEvidences: Boolean, imageFile: String? = null
    ) : this(
        id, Interval(start, end), description, billable, projectRoleId, timeUnit, hasEvidences, imageFile
    )

    fun getStart() = interval.start
    fun getEnd() = interval.end
    fun getDuration(): Int = interval.getDuration(timeUnit)
}