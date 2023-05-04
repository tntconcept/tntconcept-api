package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.Role
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.entities.WorkingAgreement
import com.autentia.tnt.binnacle.entities.WorkingAgreementTerms
import com.autentia.tnt.binnacle.entities.dto.ProjectResponseDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleUserDTO
import com.autentia.tnt.binnacle.entities.dto.RequestVacationDTO
import java.time.LocalDate

internal fun createUser(hiringDate: LocalDate): User = User(
    id = 1L,
    hiringDate = hiringDate,
    username = "jdoe",
    password = "secret",
    name = "John Doe",
    email = "jdoe@doe.com",
    dayDuration = 480,
    photoUrl = "",
    departmentId = 1L,
    role = Role(1L, "user"),
    agreementYearDuration = null,
    agreement = WorkingAgreement(
        1L, setOf(
            WorkingAgreementTerms(1, LocalDate.of(1970, 1, 1), 20, 105900),
            WorkingAgreementTerms(2, LocalDate.of(2020, 1, 1), 19, 105900),
            WorkingAgreementTerms(3, LocalDate.of(2020, 6, 1), 22, 105900),
            WorkingAgreementTerms(4, LocalDate.of(2022, 7, 1), 23, 105900)
        )
    ),
    active = true
)


internal fun createVacationRequestDTO(startDate: LocalDate, endDate: LocalDate) = RequestVacationDTO(
    id = null,
    startDate = startDate,
    endDate = endDate,
    description = "Lorem ipsum..."
)

internal fun createVacationUpdateDTO(startDate: LocalDate, endDate: LocalDate) = RequestVacationDTO(
    id = 20,
    startDate = startDate,
    endDate = endDate,
    description = "Lorem ipsum..."
)

internal fun createVacationUpdateDTO(id: Long, startDate: LocalDate, endDate: LocalDate) = RequestVacationDTO(
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

internal fun createProject(id: Long = 1L) = Project(
    id = id,
    name = "Dummy Project",
    open = true,
    billable = false,
    projectRoles = listOf(),
    organization = createOrganization()
)

internal fun createProjectResponseDTO(id: Long = 1L, open: Boolean = false, billable: Boolean = false) =
    ProjectResponseDTO(
        id = id,
        name = "Dummy Project",
        open = open,
        billable = billable,
        1L
    )

internal fun createProjectRoleUserDTO(id: Long = 1L, requireEvidence: RequireEvidence = RequireEvidence.NO) = ProjectRoleUserDTO(
    id,
    "Dummy Project Role",
    id,
    id,
    10,
    5,
    TimeUnit.DAYS,
    requireEvidence,
    true,
    id
)