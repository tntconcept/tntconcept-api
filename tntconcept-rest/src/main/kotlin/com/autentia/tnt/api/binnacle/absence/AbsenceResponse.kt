package com.autentia.tnt.api.binnacle.absence

import com.autentia.tnt.binnacle.entities.dto.AbsenceType
import java.time.LocalDate

data class AbsenceResponse(
    val type: AbsenceType,
    val startDate: LocalDate,
    val endDate: LocalDate,
)