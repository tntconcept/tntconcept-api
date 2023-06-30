package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.dto.ProjectResponseDTO
import java.time.LocalDate

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

