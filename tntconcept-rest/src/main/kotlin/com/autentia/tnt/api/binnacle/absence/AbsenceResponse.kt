package com.autentia.tnt.api.binnacle.absence

import com.autentia.tnt.binnacle.entities.dto.AbsenceDTO
import com.autentia.tnt.binnacle.entities.dto.AbsenceType
import java.time.LocalDate

data class AbsenceResponse (
    val userId: Long,
    val userName: String,
    val type: AbsenceType,
    val startDate: LocalDate,
    val endDate: LocalDate,
) {
    companion object {
        fun from(absenceDTO: AbsenceDTO) =
            AbsenceResponse(
                absenceDTO.userId,
                absenceDTO.userName,
                absenceDTO.type,
                absenceDTO.startDate,
                absenceDTO.endDate,
            )
    }
}