package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.entities.Absence
import com.autentia.tnt.binnacle.entities.dto.AbsenceDTO
import com.autentia.tnt.binnacle.entities.dto.AbsenceType
import jakarta.inject.Singleton

@Singleton
class AbsenceResponseConverter {

    fun toAbsenceDTO(absence: Absence) = AbsenceDTO(
        absence.userId,
        absence.userName,
        AbsenceType.valueOf(absence.absenceId.type),
        absence.startDate,
        absence.endDate
    )
}