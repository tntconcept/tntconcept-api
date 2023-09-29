package com.autentia.tnt.api.binnacle.absence

import com.autentia.tnt.binnacle.entities.dto.AbsenceResponseDTO

data class UserAbsenceResponse (
    val userId: Long,
    val userName: String,
    val absences: List<AbsenceResponse>
) {
    companion object {
        fun from(absenceDTO: AbsenceResponseDTO):UserAbsenceResponse {
            val absences = absenceDTO.absences.map { AbsenceResponse(it.type, it.startDate, it.endDate) }
            return UserAbsenceResponse(
                absenceDTO.userId,
                absenceDTO.userName,
                absences
            )
        }
    }
}