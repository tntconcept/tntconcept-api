package com.autentia.tnt.api.binnacle.activity

import com.autentia.tnt.binnacle.entities.dto.SubcontractedActivityFilterDTO
import io.micronaut.core.annotation.Introspected
import java.time.LocalDate

@Introspected
data class SubcontractedActivityFilterRequest(
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val organizationId: Long? = null,
    val projectId: Long? = null,
    val roleId: Long? = null,
) {
    fun toDto(): SubcontractedActivityFilterDTO =
        SubcontractedActivityFilterDTO(
            startDate,
            endDate,
            organizationId,
            projectId,
            roleId,

        )
}