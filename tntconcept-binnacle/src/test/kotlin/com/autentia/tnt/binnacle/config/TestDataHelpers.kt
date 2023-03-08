package com.autentia.tnt.binnacle.config

import com.autentia.tnt.binnacle.core.domain.ActivityResponse
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.entities.Role
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.entities.WorkingAgreement
import com.autentia.tnt.binnacle.entities.dto.*
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
    id = id,
    name = "Dummy Project role",
    requireEvidence = RequireEvidence.WEEKLY,
    project = createProject(),
    maxAllowed = 0
)

internal fun createProjectRoleResponseDTO(id: Long = 1L, requireEvidence: RequireEvidence = RequireEvidence.NO) = ProjectRoleResponseDTO(
    id = id,
    name = "Dummy Project Role",
    requireEvidence = requireEvidence
)

internal fun createActivityResponse(id: Long, startDate: LocalDateTime, hasImage: Boolean, approvalState: ApprovalState = ApprovalState.NA) = ActivityResponse(
    id = id,
    startDate = startDate,
    duration = 60,
    description = "",
    projectRole = createProjectRole(),
    userId = 1L,
    billable = false,
    organization = createOrganization(),
    project = createProject(),
    hasImage = hasImage,
    approvalState = approvalState
)

internal fun createActivityResponseDTO(id: Long, startDate: LocalDateTime, hasImage: Boolean, approvalState: ApprovalState = ApprovalState.NA) = ActivityResponseDTO(
    id = id,
    startDate = startDate,
    duration = 540,
    description = "Dummy description",
    userId = 1L,
    billable = true,
    hasImage = hasImage,
    organization = createOrganizationResponseDTO(),
    project = createProjectResponseDTO(),
    projectRole = createProjectRoleResponseDTO(),
    approvalState = approvalState
)

internal fun createActivityRequestBodyDTO(id: Long, startDate: LocalDateTime, projectRoleId: Long, hasImage: Boolean, approvalState: ApprovalState = ApprovalState.NA) =
    ActivityRequestBodyDTO(
        id,
        startDate,
        75,
        "New activity",
        false,
        projectRoleId,
        hasImage,
        approvalState = approvalState
    )



