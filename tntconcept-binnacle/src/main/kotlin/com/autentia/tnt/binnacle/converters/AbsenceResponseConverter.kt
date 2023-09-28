package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.entities.Absence
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.entities.dto.AbsenceDTO
import com.autentia.tnt.binnacle.entities.dto.AbsenceResponseDTO
import com.autentia.tnt.binnacle.entities.dto.AbsenceType
import jakarta.inject.Singleton

@Singleton
class AbsenceResponseConverter {

    fun toAbsenceResponseDTO(users: List<User>, absences: List<Absence>): List<AbsenceResponseDTO> {
        val absenceResponse = mutableListOf<AbsenceResponseDTO>()
        users.forEach { user ->
            val userAbsences = absences.filter { absence -> absence.userId == user.id }
                .map { userAbsence -> AbsenceDTO(AbsenceType.valueOf(userAbsence.absenceId.type), userAbsence.startDate, userAbsence.endDate) }
            absenceResponse.add(AbsenceResponseDTO(user.id, user.name, userAbsences))
        }
        return absenceResponse
    }
}