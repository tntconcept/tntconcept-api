package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.config.createDomainActivity
import com.autentia.tnt.binnacle.config.createDomainProjectRole
import com.autentia.tnt.binnacle.config.createProjectRoleTimeInfo
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.exception.InvalidActivityApprovalStateException
import com.autentia.tnt.binnacle.exception.NoEvidenceInActivityException
import com.autentia.tnt.binnacle.repositories.HolidayRepository
import com.autentia.tnt.binnacle.validators.ActivityValidatorTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import java.time.LocalDateTime

class ActivityTest {
    private val dateTime = LocalDateTime.of(2023, 3, 1, 13, 5, 25)
    private val dateTimePlusOneHour = dateTime.plusHours(1L)

    private val holidayRepository = mock<HolidayRepository>()

    private val calendarFactory = CalendarFactory(holidayRepository)
    private val calendar = calendarFactory.create(DateInterval.ofYear(2023))

    @Test
    fun `test isOneDay shold return true`() {
        assertTrue(
                createDomainActivity().copy(timeInterval = TimeInterval.of(dateTime, dateTimePlusOneHour)).isOneDay()
        )
    }

    @Test
    fun `test isOneDay shold return false`() {
        assertFalse(
                createDomainActivity().copy(timeInterval = TimeInterval.of(dateTime, dateTime.plusDays(1))).isOneDay()
        )
    }

    @Test
    fun `test getDurationByCountingDays in minutes `() {
        assertEquals(
                60,
                Activity.of(
                        1L,
                        TimeInterval.of(dateTime, dateTimePlusOneHour),
                        60,
                        "Description",
                        createDomainProjectRole(),
                        1L,
                        true,
                        null,
                        LocalDateTime.now(),
                        false,
                        ApprovalState.NA,
                        null,
                        null,
                        null
                ).getDurationByCountingDays(0)
        )
    }

    @Test
    fun `test getDurationByCountingDays in days `() {
        assertEquals(
                480,
                Activity.of(
                        1L,
                        TimeInterval.of(
                                dateTime,
                                dateTimePlusOneHour
                        ),
                        60,
                        "Description",
                        createDomainProjectRole().copy(timeInfo = createProjectRoleTimeInfo(timeUnit = TimeUnit.DAYS)),
                        1L,
                        true,
                        null,
                        LocalDateTime.now(),
                        false,
                        ApprovalState.NA,
                        null,
                        null,
                        null
                ).getDurationByCountingDays(1)
        )
    }

    @Test
    fun `test getDurationByCountingDays of days should return zero duration `() {
        assertEquals(
                0,
                Activity.of(
                        1L,
                        TimeInterval.of(dateTime, dateTime),
                        60,
                        "Description",
                        createDomainProjectRole(),
                        1L,
                        true,
                        null,
                        LocalDateTime.now(),
                        false,
                        ApprovalState.NA,
                        null,
                        null,
                        null
                ).getDurationByCountingDays(1)
        )
    }


    @Test
    fun `test getDurationByCountingWorkableDays in minutes `() {
        assertEquals(
                60,
                Activity.of(
                        1L,
                        TimeInterval.of(dateTime, dateTimePlusOneHour),
                        60,
                        "Description",
                        createDomainProjectRole(),
                        1L,
                        true,
                        null,
                        LocalDateTime.now(),
                        false,
                        ApprovalState.NA,
                        null,
                        null,
                        null
                ).getDurationByCountingWorkableDays(calendar)
        )
    }

    @Test
    fun `test getDurationByCountingWorkableDays in days `() {
        assertEquals(
                480,
                Activity.of(
                        1L,
                        TimeInterval.of(
                                dateTime,
                                dateTimePlusOneHour
                        ),
                        480,
                        "Description",
                        createDomainProjectRole().copy(timeInfo = createProjectRoleTimeInfo(timeUnit = TimeUnit.DAYS)),
                        1L,
                        true,
                        null,
                        LocalDateTime.now(),
                        false,
                        ApprovalState.NA,
                        null,
                        null,
                        null
                ).getDurationByCountingWorkableDays(calendar)
        )
    }


    @Test
    fun `test getDurationByCountingWorkableDays should return zero duration `() {
        assertEquals(
                0,
                Activity.of(
                        1L,
                        TimeInterval.of(dateTime, dateTime),
                        0,
                        "Description",
                        createDomainProjectRole(),
                        1L,
                        true,
                        null,
                        LocalDateTime.now(),
                        false,
                        ApprovalState.NA,
                        null,
                        null,
                        null
                ).getDurationByCountingWorkableDays(calendar)
        )
    }

    @Nested
    inner class CheckActivityIsValidForApproval {
        @Test
        fun `throw InvalidActivityApprovalStateException when activity approval state is accepted`() {
            assertThrows<InvalidActivityApprovalStateException> {
                activityWithAcceptedApprovalState.checkActivityIsValidForApproval()
            }
        }

        @Test
        fun `throw InvalidActivityApprovalStateException when activity approval state is not applicable`() {
            assertThrows<InvalidActivityApprovalStateException> {
                activityWithNotApplicableApprovalState.checkActivityIsValidForApproval()
            }
        }

        @Test
        fun `throw NoEvidenceInActivityException when activity has no evidences`() {
            assertThrows<NoEvidenceInActivityException> {
                activityWithoutEvidence.checkActivityIsValidForApproval()
            }
        }

        @Test
        fun `no exception is thrown when activity is valid for approval`() {
            assertDoesNotThrow { activityValidForApproval.checkActivityIsValidForApproval() }
        }
    }

    private companion object {
        private val activityWithAcceptedApprovalState = createDomainActivity().copy(approvalState = ApprovalState.ACCEPTED)
        private val activityWithNotApplicableApprovalState = createDomainActivity().copy(approvalState = ApprovalState.NA)
        private val activityWithoutEvidence = createDomainActivity().copy(approvalState = ApprovalState.PENDING)
        private val activityValidForApproval = createDomainActivity().copy(approvalState = ApprovalState.PENDING, hasEvidences = true)
    }
}