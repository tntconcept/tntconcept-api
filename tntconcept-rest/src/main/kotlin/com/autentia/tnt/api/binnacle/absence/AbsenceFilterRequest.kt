package com.autentia.tnt.api.binnacle.absence

import com.autentia.tnt.binnacle.entities.dto.AbsenceFilterDTO
import io.micronaut.core.annotation.Introspected
import java.time.LocalDate

@Introspected
data class  AbsenceFilterRequest (
    val startDate: LocalDate,
    val endDate: LocalDate,
    val userId: Long? = null,
    val organizationId: Long? = null,
    val projectId: Long? = null,
) {
    fun toDto(): AbsenceFilterDTO =
        AbsenceFilterDTO(
            startDate,
            endDate,
            userId,
            organizationId,
            projectId,
        )
}