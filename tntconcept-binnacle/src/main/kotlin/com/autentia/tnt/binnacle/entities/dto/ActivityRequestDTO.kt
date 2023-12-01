package com.autentia.tnt.binnacle.entities.dto

import io.micronaut.core.annotation.Introspected
import java.time.LocalDateTime
import java.util.*

@Introspected
data class ActivityRequestDTO(
    val id: Long? = null,
    val interval: TimeIntervalRequestDTO,
    val description: String,
    val billable: Boolean,
    val projectRoleId: Long,
    val evidences: List<UUID> = arrayListOf()
) {
    constructor(
        id: Long? = null,
        start: LocalDateTime,
        end: LocalDateTime,
        description: String,
        billable: Boolean,
        projectRoleId: Long,
        evidences: List<UUID> = arrayListOf()
    ) : this(id, TimeIntervalRequestDTO(start, end), description, billable, projectRoleId, evidences)

}