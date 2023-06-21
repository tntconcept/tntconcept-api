package com.autentia.tnt.binnacle.entities.dto

import io.micronaut.core.annotation.Introspected
import java.time.LocalDateTime
import jakarta.validation.constraints.Size

@Introspected
data class ActivityRequestDTO(
    val id: Long? = null,
    val interval: TimeIntervalRequestDTO,
    val description: String,
    val billable: Boolean,
    val projectRoleId: Long,
    val hasEvidences: Boolean,
    val evidence: EvidenceDTO? = null
) {
    constructor(
        id: Long? = null,
        start: LocalDateTime,
        end: LocalDateTime,
        description: String,
        billable: Boolean,
        projectRoleId: Long,
        hasEvidences: Boolean,
        evidence: EvidenceDTO? = null
    ) : this(id, TimeIntervalRequestDTO(start, end), description, billable, projectRoleId, hasEvidences, evidence)
}