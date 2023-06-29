package com.autentia.tnt.api.binnacle

import com.autentia.tnt.api.binnacle.vacation.RequestVacation
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.entities.dto.ProjectResponseDTO
import java.time.LocalDate

internal fun createVacationRequest(startDate: LocalDate, endDate: LocalDate) = RequestVacation(
    id = null,
    startDate = startDate,
    endDate = endDate,
    description = "Lorem ipsum..."
)

internal fun createVacationUpdate(startDate: LocalDate, endDate: LocalDate) = RequestVacation(
    id = 20,
    startDate = startDate,
    endDate = endDate,
    description = "Lorem ipsum..."
)

internal fun createVacationUpdate(id: Long, startDate: LocalDate, endDate: LocalDate) = RequestVacation(
    id = id,
    startDate = startDate,
    endDate = endDate,
    description = "Lorem ipsum..."
)

internal fun createOrganization(id: Long = 1L) = Organization(
    id = id,
    name = "Dummy Organization",
    projects = listOf()
)

internal fun createProjectResponseDTO(id: Long = 1L, open: Boolean = false, billable: Boolean = false) =
    ProjectResponseDTO(
        id,
        "Dummy Project",
        open,
        billable,
        1L,
        LocalDate.now(),
    )

