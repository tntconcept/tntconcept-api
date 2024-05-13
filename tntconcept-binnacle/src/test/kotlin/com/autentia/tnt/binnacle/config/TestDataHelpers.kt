package com.autentia.tnt.binnacle.config

import com.autentia.tnt.binnacle.core.domain.MaxTimeAllowed
import com.autentia.tnt.binnacle.core.domain.TimeInfo
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.entities.dto.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.util.*

internal fun createUser(): User = createUser(LocalDate.of(2020, Month.JANUARY, 1))
internal fun createUser(hiringDate: LocalDate, id: Long = 1L, name: String = "John Doe"): User = User(
    id = id,
    hiringDate = hiringDate,
    username = "jdoe",
    password = "secret",
    name = name,
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
    val vacationsEnjoyed = mutableListOf<LocalDate>()
    for (i in 1..31) {
        vacationsEnjoyed.add(LocalDate.of(2022, Month.JANUARY, i))
    }
    return vacationsEnjoyed
}


internal fun createVacationRequestDTO(startDate: LocalDate, endDate: LocalDate, chargeYear: Int = startDate.year) =
    RequestVacationDTO(
        id = null,
        startDate = startDate,
        endDate = endDate,
        chargeYear = chargeYear,
        description = "Lorem ipsum..."
    )

internal fun createVacationUpdateDTO(startDate: LocalDate, endDate: LocalDate, chargeYear: Int = startDate.year) =
    RequestVacationDTO(
        id = 20,
        startDate = startDate,
        endDate = endDate,
        chargeYear = chargeYear,
        description = "Lorem ipsum..."
    )

internal fun createVacationUpdateDTO(
    id: Long,
    startDate: LocalDate,
    endDate: LocalDate,
    chargeYear: Int = startDate.year
) = RequestVacationDTO(
    id = id,
    startDate = startDate,
    endDate = endDate,
    chargeYear = chargeYear,
    description = "Lorem ipsum..."
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

internal fun createVacation(
    id: Long = 1L,
    startDate: LocalDate,
    endDate: LocalDate,
    state: VacationState = VacationState.PENDING,
    userId: Long = 1L,
    observations: String = "Dummy observations",
    departmentId: Long? = null,
    description: String = "Dummy description",
    chargeYear: LocalDate = LocalDate.of(LocalDate.now().year, 1, 1),
): Vacation =
    Vacation(
        id,
        startDate,
        endDate,
        state,
        userId,
        observations,
        departmentId,
        description,
        chargeYear,
    )

internal fun createOrganization(id: Long = 1L) = Organization(
    id = id,
    name = "Dummy Organization",
    organizationTypeId = 1L,
    projects = listOf()
)

internal fun createProject(id: Long = 1L) = Project(
    id = id,
    name = "Dummy Project",
    open = true,
    LocalDate.now(),
    null,
    null,
    projectRoles = listOf(),
    organization = createOrganization(),
    billingType = "TIME_AND_MATERIALS"
)

internal fun createBlockedProject(id: Long = 1L) = Project(
    id = id,
    name = "Dummy Project",
    open = true,
    LocalDate.now(),
    blockDate = LocalDate.of(2000, 1, 1),
    null,
    projectRoles = listOf(),
    organization = createOrganization(),
    billingType = "NO_BILLABLE"
)

internal fun createProjectRoleTimeInfo(
    maxTimeAllowedByYear: Int = 0,
    maxTimeAllowedByActivity: Int = 0,
    timeUnit: TimeUnit = TimeUnit.MINUTES,
) =
    TimeInfo(MaxTimeAllowed(maxTimeAllowedByYear, maxTimeAllowedByActivity), timeUnit)

internal fun createProjectRole(id: Long = 1L): ProjectRole = ProjectRole(
    id,
    "Dummy Project role",
    RequireEvidence.WEEKLY,
    createProject(),
    0,
    0,
    true,
    false,
    TimeUnit.MINUTES
)

internal fun createProjectRole(id: Long = 1L, project: Project): ProjectRole = ProjectRole(
    id,
    "Dummy Project role",
    RequireEvidence.WEEKLY,
    project,
    0,
    0,
    true,
    false,
    TimeUnit.MINUTES
)

internal fun createProjectRoleWithTimeUnit(id: Long = 1L, timeUnit: TimeUnit): ProjectRole = ProjectRole(
    id,
    "Dummy Project role in days",
    RequireEvidence.NO,
    createProject(),
    0,
    0,
    true,
    false,
    timeUnit
)

internal fun createActivity(id: Long? = 1, approvalState: ApprovalState = ApprovalState.NA) = Activity(
    id,
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
    approvalState
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

internal fun createDomainActivity(
    start: LocalDateTime = LocalDateTime.of(2023, 3, 1, 13, 5, 25),
    end: LocalDateTime = LocalDateTime.of(2023, 3, 1, 13, 5, 25).plusHours(1),
    duration: Int = 60,
    projectRole: com.autentia.tnt.binnacle.core.domain.ProjectRole = createDomainProjectRole(),
) =
    com.autentia.tnt.binnacle.core.domain.Activity.of(
        1L,
        TimeInterval.of(
            start,
            end
        ),
        duration,
        "Description",
        projectRole,
        1L,
        true,
        null,
        null,
        false,
        ApprovalState.NA,
        null,
        null,
        null
    )

internal fun createDomainProjectRole() = createProjectRole().toDomain()

internal fun createDomainUser(
    id: Long = 1L,
    name: String = "John Doe",
    hiringDate: LocalDate = LocalDate.of(2020, Month.JANUARY, 1),
) =
    createUser(hiringDate, id, name).toDomain()

internal fun createActivityResponseDTO(
    id: Long,
    start: LocalDateTime,
    end: LocalDateTime,
    hasEvidences: Boolean,
    approvalState: ApprovalState = ApprovalState.NA,
) = ActivityResponseDTO(
    billable = true,
    description = "Dummy description",
    hasEvidences = hasEvidences,
    id = id,
    projectRoleId = 1L,
    interval = IntervalResponseDTO(start, end, 45, TimeUnit.MINUTES),
    userId = 1L,
    approval = ApprovalDTO(approvalState)
)

internal fun createAttachmentInfoEntityWithFilenameAndMimetype(filename: String, mimeType: String) = AttachmentInfo(
    id = UUID.randomUUID(),
    userId = 1L,
    path = "/",
    fileName = filename,
    mimeType = mimeType,
    uploadDate = LocalDateTime.now().withSecond(0).withNano(0),
    isTemporary = true
)

internal fun createAbsence(id: Long, type: String) = Absence(
    AbsenceId(id, type),
    1L,
    LocalDate.of(2023, Month.JANUARY, 1),
    LocalDate.of(2023, Month.JANUARY, 3)
)