package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.*
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AbsenceDaoIT {

    private lateinit var projectRolePaidLeave1: ProjectRole
    private lateinit var projectRolePaidLeave2: ProjectRole
    private lateinit var otherProjectRole: ProjectRole

    @Inject
    private lateinit var vacationDao: VacationDao

    @Inject
    private lateinit var activityDao: ActivityDao

    @Inject
    private lateinit var projectRoleDao: ProjectRoleDao

    @Inject
    private lateinit var absenceDao: AbsenceDao

    @BeforeAll
    fun `obtain data test references`() {
        projectRolePaidLeave1 = projectRoleDao.findById(paidLeaveProjectRoleId1).orElseThrow { IllegalStateException() }
        projectRolePaidLeave2 = projectRoleDao.findById(paidLeaveProjectRoleId2).orElseThrow { IllegalStateException() }
        otherProjectRole = projectRoleDao.findById(otherProjectRoleId).orElseThrow { IllegalStateException() }
    }

    @Test
    fun `should recover absences between two dates`() {
        val startDate = LocalDate.of(2023, Month.AUGUST, 1)
        val endDate = LocalDate.of(2023, Month.SEPTEMBER, 30)
        val activitiesToSave = listOf(
            Activity(
                start = LocalDateTime.of(2023, 9, 1, 9, 0, 0),
                end = LocalDateTime.of(2023, 9, 1, 17, 0, 0),
                duration = 480,
                description = "Absence 1",
                projectRole = projectRolePaidLeave2,
                userId = 11,
                billable = false,
                hasEvidences = false,
                approvalState = ApprovalState.PENDING
            ),
            Activity(
                start = LocalDateTime.of(2023, 9, 2, 9, 0, 0),
                end = LocalDateTime.of(2023, 9, 2, 13, 0, 0),
                duration = 240,
                description = "Absence 2",
                projectRole = projectRolePaidLeave2,
                userId = 11,
                billable = false,
                hasEvidences = false,
                approvalState = ApprovalState.PENDING
            ),
            Activity(
                start = LocalDateTime.of(2023, 9, 2, 13, 0, 0),
                end = LocalDateTime.of(2023, 9, 2, 17, 0, 0),
                duration = 240,
                description = "Absence 3",
                projectRole = otherProjectRole,
                userId = 11,
                billable = false,
                hasEvidences = false,
                approvalState = ApprovalState.PENDING
            ),
            Activity(
                start = LocalDateTime.of(2023, 9, 3, 9, 0, 0),
                end = LocalDateTime.of(2023, 9, 3, 17, 0, 0),
                duration = 480,
                description = "Absence 4",
                projectRole = otherProjectRole,
                userId = 11,
                billable = false,
                hasEvidences = false,
                approvalState = ApprovalState.PENDING
            ),
            Activity(
                start = LocalDateTime.of(2023, 9, 6, 0, 0, 0),
                end = LocalDateTime.of(2023, 9, 7, 23, 59, 59),
                duration = 960,
                description = "Absence 5",
                projectRole = projectRolePaidLeave1,
                userId = 11,
                billable = false,
                hasEvidences = false,
                approvalState = ApprovalState.PENDING
            ),
            Activity(
                start = LocalDateTime.of(2023, 9, 7, 9, 0, 0),
                end = LocalDateTime.of(2023, 9, 7, 17, 0, 0),
                duration = 240,
                description = "Absence 6",
                projectRole = otherProjectRole,
                userId = 11,
                billable = false,
                hasEvidences = false,
                approvalState = ApprovalState.PENDING
            ),
            Activity(
                start = LocalDateTime.of(2023, 9, 4, 9, 0, 0),
                end = LocalDateTime.of(2023, 9, 4, 17, 0, 0),
                duration = 480,
                description = "Absence 7",
                projectRole = projectRolePaidLeave2,
                userId = 120,
                billable = false,
                hasEvidences = false,
                approvalState = ApprovalState.PENDING
            ),
        )
        val vacationsToSave = listOf(
            Vacation(
                id = null,
                startDate = LocalDate.of(2023, 8, 30),
                endDate = LocalDate.of(2023, 8, 31),
                chargeYear = LocalDate.of(2023, 1, 31),
                userId = 11,
                description = "Vacations 1",
                state = VacationState.ACCEPT
            ),
            Vacation(
                id = null,
                startDate = LocalDate.of(2023, 2, 2),
                endDate = LocalDate.of(2023, 2, 5),
                chargeYear = LocalDate.of(2023, 1, 31),
                userId = 120,
                description = "Vacations 2",
                state = VacationState.ACCEPT
            ),
        )


        val savedActivities = activityDao.saveAll(activitiesToSave)
        val savedVacations = vacationDao.saveAll(vacationsToSave)
        val expectedAbsences = listOf(
            Absence(AbsenceId(savedVacations.elementAt(0).id!!, "VACATION"), 11, LocalDate.of(2023, 8, 30), LocalDate.of(2023, 8, 31)),
            Absence(AbsenceId(savedActivities.elementAt(0).id!!, "PAID_LEAVE"), 11, LocalDate.of(2023, 9, 1), LocalDate.of(2023, 9, 1)),
            Absence(AbsenceId(savedActivities.elementAt(6).id!!, "PAID_LEAVE"), 120, LocalDate.of(2023, 9, 4), LocalDate.of(2023, 9, 4)),
            Absence(AbsenceId(savedActivities.elementAt(4).id!!, "PAID_LEAVE"), 11, LocalDate.of(2023, 9, 6), LocalDate.of(2023, 9, 7)),
        )

        val result = absenceDao.findAllByDateBetweenAndUsers(startDate, endDate, null)

        assertEquals(expectedAbsences, result)
    }

    @Test
    fun `should recover absences between two dates and by user ids`() {
        val startDate = LocalDate.of(2023, Month.AUGUST, 1)
        val endDate = LocalDate.of(2023, Month.SEPTEMBER, 30)
        val activitiesToSave = listOf(
            Activity(
                start = LocalDateTime.of(2023, 9, 1, 9, 0, 0),
                end = LocalDateTime.of(2023, 9, 1, 17, 0, 0),
                duration = 480,
                description = "Absence 1",
                projectRole = projectRolePaidLeave2,
                userId = 11,
                billable = false,
                hasEvidences = false,
                approvalState = ApprovalState.PENDING
            ),
            Activity(
                start = LocalDateTime.of(2023, 9, 2, 9, 0, 0),
                end = LocalDateTime.of(2023, 9, 2, 13, 0, 0),
                duration = 240,
                description = "Absence 2",
                projectRole = projectRolePaidLeave2,
                userId = 11,
                billable = false,
                hasEvidences = false,
                approvalState = ApprovalState.PENDING
            ),
            Activity(
                start = LocalDateTime.of(2023, 9, 2, 13, 0, 0),
                end = LocalDateTime.of(2023, 9, 2, 17, 0, 0),
                duration = 240,
                description = "Absence 3",
                projectRole = otherProjectRole,
                userId = 11,
                billable = false,
                hasEvidences = false,
                approvalState = ApprovalState.PENDING
            ),
            Activity(
                start = LocalDateTime.of(2023, 9, 3, 9, 0, 0),
                end = LocalDateTime.of(2023, 9, 3, 17, 0, 0),
                duration = 480,
                description = "Absence 4",
                projectRole = otherProjectRole,
                userId = 11,
                billable = false,
                hasEvidences = false,
                approvalState = ApprovalState.PENDING
            ),
            Activity(
                start = LocalDateTime.of(2023, 9, 6, 0, 0, 0),
                end = LocalDateTime.of(2023, 9, 7, 23, 59, 59),
                duration = 960,
                description = "Absence 5",
                projectRole = projectRolePaidLeave1,
                userId = 11,
                billable = false,
                hasEvidences = false,
                approvalState = ApprovalState.PENDING
            ),
            Activity(
                start = LocalDateTime.of(2023, 9, 7, 9, 0, 0),
                end = LocalDateTime.of(2023, 9, 7, 17, 0, 0),
                duration = 240,
                description = "Absence 6",
                projectRole = otherProjectRole,
                userId = 11,
                billable = false,
                hasEvidences = false,
                approvalState = ApprovalState.PENDING
            ),
            Activity(
                start = LocalDateTime.of(2023, 9, 4, 9, 0, 0),
                end = LocalDateTime.of(2023, 9, 4, 17, 0, 0),
                duration = 480,
                description = "Absence 7",
                projectRole = projectRolePaidLeave2,
                userId = 120,
                billable = false,
                hasEvidences = false,
                approvalState = ApprovalState.PENDING
            ),
            Activity(
                start = LocalDateTime.of(2023, 9, 4, 9, 0, 0),
                end = LocalDateTime.of(2023, 9, 4, 17, 0, 0),
                duration = 480,
                description = "Absence 8",
                projectRole = projectRolePaidLeave2,
                userId = 1,
                billable = false,
                hasEvidences = false,
                approvalState = ApprovalState.PENDING
            ),
        )
        val vacationsToSave = listOf(
            Vacation(
                id = null,
                startDate = LocalDate.of(2023, 8, 30),
                endDate = LocalDate.of(2023, 8, 31),
                chargeYear = LocalDate.of(2023, 1, 31),
                userId = 11,
                description = "Vacations 1",
                state = VacationState.ACCEPT
            ),
            Vacation(
                id = null,
                startDate = LocalDate.of(2023, 2, 2),
                endDate = LocalDate.of(2023, 2, 5),
                chargeYear = LocalDate.of(2023, 1, 31),
                userId = 120,
                description = "Vacations 2",
                state = VacationState.ACCEPT
            ),
            Vacation(
                id = null,
                startDate = LocalDate.of(2023, 9, 1),
                endDate = LocalDate.of(2023, 9, 2),
                chargeYear = LocalDate.of(2023, 1, 31),
                userId = 1,
                description = "Vacations 3",
                state = VacationState.PENDING
            ),
        )

        val savedActivities = activityDao.saveAll(activitiesToSave)
        val savedVacations = vacationDao.saveAll(vacationsToSave)
        val expectedAbsences = listOf(
            Absence(AbsenceId(savedVacations.elementAt(0).id!!, "VACATION"), 11, LocalDate.of(2023, 8, 30), LocalDate.of(2023, 8, 31)),
            Absence(AbsenceId(savedActivities.elementAt(0).id!!, "PAID_LEAVE"), 11, LocalDate.of(2023, 9, 1), LocalDate.of(2023, 9, 1)),
            Absence(AbsenceId(savedActivities.elementAt(6).id!!, "PAID_LEAVE"), 120, LocalDate.of(2023, 9, 4), LocalDate.of(2023, 9, 4)),
            Absence(AbsenceId(savedActivities.elementAt(4).id!!, "PAID_LEAVE"), 11, LocalDate.of(2023, 9, 6), LocalDate.of(2023, 9, 7)),
        )

        val result = absenceDao.findAllByDateBetweenAndUsers(startDate, endDate, listOf(11, 120))

        assertEquals(expectedAbsences, result)
    }

    @Test
    fun `should recover absences between two dates and by user id`() {
        val startDate = LocalDate.of(2023, Month.AUGUST, 27)
        val endDate = LocalDate.of(2023, Month.SEPTEMBER, 30)
        val activitiesToSave = listOf(
            Activity(
                start = LocalDateTime.of(2023, 9, 1, 9, 0, 0),
                end = LocalDateTime.of(2023, 9, 1, 17, 0, 0),
                duration = 480,
                description = "Absence 1",
                projectRole = projectRolePaidLeave2,
                userId = 11,
                billable = false,
                hasEvidences = false,
                approvalState = ApprovalState.PENDING
            ),
            Activity(
                start = LocalDateTime.of(2023, 9, 2, 9, 0, 0),
                end = LocalDateTime.of(2023, 9, 2, 13, 0, 0),
                duration = 240,
                description = "Absence 2",
                projectRole = projectRolePaidLeave2,
                userId = 11,
                billable = false,
                hasEvidences = false,
                approvalState = ApprovalState.PENDING
            ),
            Activity(
                start = LocalDateTime.of(2023, 9, 2, 13, 0, 0),
                end = LocalDateTime.of(2023, 9, 2, 17, 0, 0),
                duration = 240,
                description = "Absence 3",
                projectRole = otherProjectRole,
                userId = 11,
                billable = false,
                hasEvidences = false,
                approvalState = ApprovalState.PENDING
            ),
            Activity(
                start = LocalDateTime.of(2023, 9, 3, 9, 0, 0),
                end = LocalDateTime.of(2023, 9, 3, 17, 0, 0),
                duration = 480,
                description = "Absence 4",
                projectRole = otherProjectRole,
                userId = 11,
                billable = false,
                hasEvidences = false,
                approvalState = ApprovalState.PENDING
            ),
            Activity(
                start = LocalDateTime.of(2023, 9, 6, 0, 0, 0),
                end = LocalDateTime.of(2023, 9, 7, 23, 59, 59),
                duration = 960,
                description = "Absence 5",
                projectRole = projectRolePaidLeave1,
                userId = 11,
                billable = false,
                hasEvidences = false,
                approvalState = ApprovalState.PENDING
            ),
            Activity(
                start = LocalDateTime.of(2023, 9, 7, 9, 0, 0),
                end = LocalDateTime.of(2023, 9, 7, 17, 0, 0),
                duration = 240,
                description = "Absence 6",
                projectRole = otherProjectRole,
                userId = 11,
                billable = false,
                hasEvidences = false,
                approvalState = ApprovalState.PENDING
            ),
            Activity(
                start = LocalDateTime.of(2023, 9, 4, 9, 0, 0),
                end = LocalDateTime.of(2023, 9, 4, 17, 0, 0),
                duration = 480,
                description = "Absence 7",
                projectRole = projectRolePaidLeave2,
                userId = 120,
                billable = false,
                hasEvidences = false,
                approvalState = ApprovalState.PENDING
            ),
            Activity(
                start = LocalDateTime.of(2023, 9, 4, 9, 0, 0),
                end = LocalDateTime.of(2023, 9, 4, 17, 0, 0),
                duration = 480,
                description = "Absence 8",
                projectRole = projectRolePaidLeave2,
                userId = 1,
                billable = false,
                hasEvidences = false,
                approvalState = ApprovalState.PENDING
            ),
        )
        val vacationsToSave = listOf(
            Vacation(
                id = null,
                startDate = LocalDate.of(2023, 8, 21),
                endDate = LocalDate.of(2023, 8, 31),
                chargeYear = LocalDate.of(2023, 1, 31),
                userId = 11,
                description = "Vacations 1",
                state = VacationState.ACCEPT
            ),
            Vacation(
                id = null,
                startDate = LocalDate.of(2023, 2, 2),
                endDate = LocalDate.of(2023, 2, 5),
                chargeYear = LocalDate.of(2023, 1, 31),
                userId = 120,
                description = "Vacations 2",
                state = VacationState.ACCEPT
            ),
            Vacation(
                id = null,
                startDate = LocalDate.of(2023, 9, 1),
                endDate = LocalDate.of(2023, 9, 2),
                chargeYear = LocalDate.of(2023, 1, 31),
                userId = 1,
                description = "Vacations 3",
                state = VacationState.PENDING
            ),
        )

        val savedActivities = activityDao.saveAll(activitiesToSave)
        val savedVacations = vacationDao.saveAll(vacationsToSave)
        val expectedAbsences = listOf(
            Absence(AbsenceId(savedVacations.elementAt(0).id!!, "VACATION"), 11, LocalDate.of(2023, 8, 21), LocalDate.of(2023, 8, 31)),
            Absence(AbsenceId(savedActivities.elementAt(0).id!!, "PAID_LEAVE"), 11, LocalDate.of(2023, 9, 1), LocalDate.of(2023, 9, 1)),
            Absence(AbsenceId(savedActivities.elementAt(4).id!!, "PAID_LEAVE"), 11, LocalDate.of(2023, 9, 6), LocalDate.of(2023, 9, 7)),
        )

        val result = absenceDao.findAllByDateBetweenAndUser(startDate, endDate, 11)

        assertEquals(expectedAbsences, result)
    }

    private companion object {
        private const val paidLeaveProjectRoleId1 = 13L
        private const val paidLeaveProjectRoleId2 = 13L

        private const val otherProjectRoleId = 5L
    }

}
