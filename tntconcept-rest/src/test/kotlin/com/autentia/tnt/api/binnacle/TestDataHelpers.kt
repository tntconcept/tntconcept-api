package com.autentia.tnt.api.binnacle

import com.autentia.tnt.api.binnacle.vacation.VacationResponse
import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.VacationState
import com.autentia.tnt.binnacle.entities.dto.VacationDTO
import java.time.LocalDate

internal fun createOrganization(id: Long = 1L) = Organization(
    id = id,
    name = "Dummy Organization",
    organizationTypeId = 1,
    projects = listOf()
)

internal fun createVacationDTO(
    id: Long = 1L,
    observations: String = "Dummy observations",
    description: String = "Dummy description",
    state: VacationState = VacationState.PENDING,
    startDate: LocalDate = LocalDate.of(LocalDate.now().year, 1, 1),
    endDate: LocalDate = LocalDate.of(LocalDate.now().year, 12, 31),
    days: List<LocalDate> = emptyList(),
    chargeYear: LocalDate = LocalDate.of(LocalDate.now().year, 1, 1),
): VacationDTO =
    VacationDTO(
        id,
        observations,
        description,
        state,
        startDate,
        endDate,
        days,
        chargeYear,
    )

internal fun createVacationResponse(
    id: Long = 1L,
    observations: String = "Dummy observations",
    description: String = "Dummy description",
    state: VacationState = VacationState.PENDING,
    startDate: LocalDate = LocalDate.of(LocalDate.now().year, 1, 1),
    endDate: LocalDate = LocalDate.of(LocalDate.now().year, 12, 31),
    days: List<LocalDate> = emptyList(),
    chargeYear: LocalDate = LocalDate.of(LocalDate.now().year, 1, 1),
): VacationResponse =
    VacationResponse(
        id,
        observations,
        description,
        state,
        startDate,
        endDate,
        days,
        chargeYear,
    )


