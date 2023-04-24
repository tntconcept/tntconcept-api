package com.autentia.tnt.binnacle.config

import com.autentia.tnt.binnacle.core.domain.ActivityResponse
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.Holiday
import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.Role
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.entities.WorkingAgreement
import com.autentia.tnt.binnacle.entities.WorkingAgreementTerms
import com.autentia.tnt.binnacle.entities.dto.ActivityRequestBodyDTO
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.entities.dto.IntervalResponseDTO
import com.autentia.tnt.binnacle.entities.dto.OrganizationResponseDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectResponseDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleUserDTO
import com.autentia.tnt.binnacle.entities.dto.RequestVacationDTO
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month

internal fun createUser(): User = createUser(LocalDate.of(2020, Month.JANUARY, 1))
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

internal fun createHoliday() = Holiday(1, "Holiday description", LocalDate.of(2023, 3, 15).atStartOfDay())


internal fun getHolidaysFrom2022(): List<LocalDate> = listOf<LocalDate>(
    LocalDate.of(2022, Month.JANUARY, 1),
    LocalDate.of(2022, Month.JANUARY, 6),
    LocalDate.of(2022, Month.APRIL, 14),
    LocalDate.of(2022, Month.APRIL, 15),
    LocalDate.of(2022, Month.MAY, 2),
    LocalDate.of(2022, Month.MAY, 16),
    LocalDate.of(2022, Month.JULY, 25),
    LocalDate.of(2022, Month.AUGUST, 15),
    LocalDate.of(2022, Month.OCTOBER, 12),
    LocalDate.of(2022, Month.NOVEMBER, 1),
    LocalDate.of(2022, Month.NOVEMBER, 9),
    LocalDate.of(2022, Month.DECEMBER, 6),
    LocalDate.of(2022, Month.DECEMBER, 8),
    LocalDate.of(2022, Month.DECEMBER, 26),
)

internal fun getVacationsInOneMonth2022(): List<LocalDate> {
    val vacationsRequested = mutableListOf<LocalDate>()
    for (i in 1..31) {
          vacationsRequested.add(LocalDate.of(2022, Month.JANUARY, i))
    }
    return vacationsRequested
}


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

internal fun createOrganizationResponseDTO(id: Long = 1L) = OrganizationResponseDTO(
    id = id,
    name = "Dummy Organization",
)

internal fun createProject(id: Long = 1L) = Project(
    id = id,
    name = "Dummy Project",
    open = true,
    billable = false,
    projectRoles = listOf(),
    organization = createOrganization()
)

internal fun createProjectResponseDTO(id: Long = 1L, open: Boolean = false, billable: Boolean = false) = ProjectResponseDTO(
    id = id,
    name = "Dummy Project",
    open = open,
    billable = billable
)

internal fun createProjectRole(id: Long = 1L): ProjectRole = ProjectRole(
    id,
    "Dummy Project role",
    RequireEvidence.WEEKLY,
    createProject(),
    0,
    true,
    false,
    TimeUnit.MINUTES
)

internal fun createActivity() = Activity(
    1,
    LocalDateTime.of(2023, 3, 1, 13, 5, 25),
    LocalDateTime.of(2023, 3, 1, 13, 5, 25).plusHours(1),
    60,
    "Activity",
    createProjectRole(),
    1L,
    true,
    1L,
    null,
    false,
    ApprovalState.NA
)

internal fun createActivity(projectRole: ProjectRole) = Activity(
    1,
    LocalDateTime.of(2023, 3, 1, 13, 5, 25),
    LocalDateTime.of(2023, 3, 1, 13, 5, 25).plusHours(1),
    60,
    "Activity",
    projectRole,
    1L,
    true,
    1L,
    null,
    false,
    ApprovalState.NA
)

internal fun createDomainActivity() = com.autentia.tnt.binnacle.core.domain.Activity(
    LocalDateTime.of(2023, 3, 1, 13, 5, 25),
    LocalDateTime.of(2023, 3, 1, 13, 5, 25).plusHours(1),
    createDomainProjectRole(),
    1L
)

internal fun createDomainProjectRole() = createProjectRole().toDomain()

internal fun createProjectRoleResponseDTO(id: Long = 1L, requireEvidence: RequireEvidence = RequireEvidence.NO) =
    ProjectRoleUserDTO(
        id = id,
        name = "Dummy Project Role",
        1L,
        1L,
        10,
        5,
        TimeUnit.MINUTES,
        requireEvidence = requireEvidence,
        false,
        1L
    )


internal fun createActivityResponse(
    id: Long,
    start: LocalDateTime,
    end: LocalDateTime,
    hasEvidences: Boolean,
    approvalState: ApprovalState = ApprovalState.NA
) = ActivityResponse(
    id = id,
    start = start,
    end = end,
    duration = 60,
    description = "",
    projectRole = createProjectRole(),
    userId = 1L,
    billable = false,
    organization = createOrganization(),
    project = createProject(),
    hasEvidences = hasEvidences,
    approvalState = approvalState
)

internal fun createActivityResponseDTO(
    id: Long,
    start: LocalDateTime,
    end: LocalDateTime,
    hasEvidences: Boolean,
    approvalState: ApprovalState = ApprovalState.NA
) = ActivityResponseDTO(
    billable = true,
    description = "Dummy description",
    hasEvidences = hasEvidences,
    id = id,
    projectRoleId = 1L,
    interval = IntervalResponseDTO(start, end,45, TimeUnit.MINUTES),
    userId = 1L,
    approvalState = approvalState
)

internal fun createActivityRequestBodyDTO(
    id: Long,
    start: LocalDateTime,
    end: LocalDateTime,
    projectRoleId: Long,
    hasEvidences: Boolean
) =
    ActivityRequestBodyDTO(
        id,
        start,
        end,
        "New activity",
        false,
        projectRoleId,
        hasEvidences,
    )




