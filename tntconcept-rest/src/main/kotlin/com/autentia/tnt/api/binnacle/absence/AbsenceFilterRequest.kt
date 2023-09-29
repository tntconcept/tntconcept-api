package com.autentia.tnt.api.binnacle.absence

import com.autentia.tnt.binnacle.entities.dto.AbsenceFilterDTO
import io.micronaut.core.annotation.Introspected
import java.time.LocalDate

@Introspected
data class  AbsenceFilterRequest (
    val startDate: LocalDate,
    val endDate: LocalDate,
    val userIds: String? = null,
    val organizationIds: String? = null,
    val projectIds: String? = null,
) {
    fun toDto(): AbsenceFilterDTO =
        AbsenceFilterDTO(
            startDate,
            endDate,
            userIds?.split(",".trim())?.map { it.toLong() },
            organizationIds?.split(",".trim())?.map { it.toLong() },
            projectIds?.split(",".trim())?.map { it.toLong() },
        )
}