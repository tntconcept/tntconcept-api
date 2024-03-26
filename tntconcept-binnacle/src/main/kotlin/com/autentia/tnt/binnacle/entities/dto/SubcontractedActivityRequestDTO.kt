package com.autentia.tnt.binnacle.entities.dto


import io.micronaut.core.annotation.Introspected
import java.time.LocalDateTime
@Introspected
data class SubcontractedActivityRequestDTO (
        val id: Long? = null,
        val interval: TimeIntervalRequestDTO,
        val duration: Int,
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
            duration: Int,
            description: String,
            billable: Boolean,
            projectRoleId: Long,
            hasEvidences: Boolean,
            evidence: EvidenceDTO? = null
    ) : this(id, TimeIntervalRequestDTO(start, end), duration, description, billable, projectRoleId, hasEvidences, evidence)
}