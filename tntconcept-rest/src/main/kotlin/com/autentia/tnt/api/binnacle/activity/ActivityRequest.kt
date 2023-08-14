package com.autentia.tnt.api.binnacle.activity

import com.autentia.tnt.binnacle.entities.dto.ActivityRequestDTO
import com.autentia.tnt.binnacle.entities.dto.TimeIntervalRequestDTO
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.Size

@Introspected
data class ActivityRequest(
    val id: Long? = null,
    val interval: TimeIntervalRequest,
    @field:Size(max = 2048, message = "Description must not exceed 2048 characters")
    val description: String,
    val billable: Boolean,
    val projectRoleId: Long,
    val evidences: List<UUID> = arrayListOf()
) {
    fun toDto(): ActivityRequestDTO = ActivityRequestDTO(
        id,
        TimeIntervalRequestDTO(interval.start, interval.end),
        description,
        billable,
        projectRoleId,
        evidences = arrayListOf()
    )

    fun hasEvidences() = evidences.isNotEmpty()
}