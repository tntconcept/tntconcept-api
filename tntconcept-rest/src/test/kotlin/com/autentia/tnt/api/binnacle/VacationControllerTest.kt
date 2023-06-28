package com.autentia.tnt.api.binnacle

import com.autentia.tnt.api.binnacle.vacation.HolidayResponse
import com.autentia.tnt.api.binnacle.vacation.VacationController
import com.autentia.tnt.binnacle.entities.VacationState
import com.autentia.tnt.binnacle.entities.VacationState.ACCEPT
import com.autentia.tnt.binnacle.entities.VacationState.PENDING
import com.autentia.tnt.binnacle.entities.dto.*
import com.autentia.tnt.binnacle.exception.*
import com.autentia.tnt.binnacle.usecases.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.BDDMockito.willDoNothing
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.Month
import java.util.Locale.ENGLISH

internal class VacationControllerTest {

    private val privateHolidaysByChargeYearUseCase = mock<PrivateHolidaysByChargeYearUseCase>()
    private val privateHolidayDetailsUseCase = mock<PrivateHolidayDetailsUseCase>()
    private val privateHolidaysPeriodDaysUseCase = mock<PrivateHolidaysPeriodDaysUseCase>()
    private val privateHolidayPeriodCreateUseCase = mock<PrivateHolidayPeriodCreateUseCase>()
    private val privateHolidayPeriodUpdateUseCase = mock<PrivateHolidayPeriodUpdateUseCase>()
    private val privateHolidayPeriodDeleteUseCase = mock<PrivateHolidayPeriodDeleteUseCase>()

    private val vacationController = VacationController(
        privateHolidaysByChargeYearUseCase,
        privateHolidayDetailsUseCase,
        privateHolidaysPeriodDaysUseCase,
        privateHolidayPeriodCreateUseCase,
        privateHolidayPeriodUpdateUseCase,
        privateHolidayPeriodDeleteUseCase
    )

    @Test
    fun `get the vacations by charge year`() {
        val chargeYear = 2019
        val holidayResponseDTO = HolidayResponseDTO(listOf(), listOf())
        val holidayResponse = HolidayResponse.from(holidayResponseDTO)

        doReturn(holidayResponseDTO).whenever(privateHolidaysByChargeYearUseCase).get(chargeYear)

        val response = vacationController.getPrivateHolidaysByChargeYear(chargeYear)

        assertEquals(holidayResponse, response)
    }

    @Test
    fun `get ALL vacations DAYS between date`() {

        val workingDaysBetweenDates = 8

        doReturn(workingDaysBetweenDates).whenever(privateHolidaysPeriodDaysUseCase).get(today, today.plusDays(8))

        val holidaysBetweenDates = vacationController.getPrivateHolidaysPeriodDays(today, today.plusDays(8))

        assertEquals(workingDaysBetweenDates, holidaysBetweenDates)

    }

    @Test
    fun `get user vacation details`() {
        val chargeYear = 2020
        val correspondingVacations = 22

        val holidayResponseDTO = HolidayResponseDTO(HOLIDAYS_DTO, VACATIONS_DTO)

        val acceptedVacations = VACATIONS_DTO.filter { it.state == ACCEPT || it.state == PENDING }.size

        doReturn(holidayResponseDTO).whenever(privateHolidaysByChargeYearUseCase).get(chargeYear)

        val response = VacationDetailsDTO(
            holidaysAgreement = correspondingVacations,
            correspondingVacations = correspondingVacations,
            acceptedVacations = acceptedVacations,
            remainingVacations = correspondingVacations - acceptedVacations
        )

        doReturn(response).whenever(privateHolidayDetailsUseCase).get(chargeYear, holidayResponseDTO.vacations)

        val vacationDetailsDTO = vacationController.getPrivateHolidayDetails(chargeYear)

        assertEquals(response, vacationDetailsDTO)
        verify(privateHolidaysByChargeYearUseCase, times(1)).get(chargeYear)

    }

    @Nested
    inner class CreateVacationPeriod {
        @Test
        fun `create new vacation period`() {

            val chargeYear = LocalDate.now().year

            val requestVacationDTO = RequestVacationDTO(
                id = 1L,
                startDate = today,
                endDate = tomorrow,
                description = "Lorem ipsum..."
            )
            val vacationResponseDTO = CreateVacationResponseDTO(
                startDate = today,
                endDate = tomorrow,
                days = 1,
                chargeYear = chargeYear
            )

            doReturn(listOf(vacationResponseDTO)).whenever(privateHolidayPeriodCreateUseCase).create(any(), any())

            val createVacationResponseDTO = vacationController.createPrivateHolidayPeriod(requestVacationDTO, ENGLISH)

            assertEquals(listOf(vacationResponseDTO), createVacationResponseDTO)
            verify(privateHolidayPeriodCreateUseCase, times(1)).create(requestVacationDTO, ENGLISH)

        }

        @Test
        fun `FAIL to create when the start date is LATER than end date`() {

            val createVacationRequestDTO = createVacationRequestDTO(startDate = tomorrow, endDate = today)

            doThrow(DateRangeException(startDate = tomorrow, endDate = today))
                .whenever(privateHolidayPeriodCreateUseCase)
                .create(eq(createVacationRequestDTO(startDate = tomorrow, endDate = today)), any())

            assertThrows<DateRangeException> {
                vacationController.createPrivateHolidayPeriod(createVacationRequestDTO, ENGLISH)
            }
            verify(privateHolidayPeriodCreateUseCase, times(1)).create(createVacationRequestDTO, ENGLISH)
        }

        @Test
        fun `FAIL when more than 5 days of NEXT year's vacation are requested for the current year`() {

            val createVacationRequestDTO = createVacationRequestDTO(today, today.plusDays(5))

            doThrow(MaxNextYearRequestVacationException("You can't charge more than 5 days of the next year vacations in the current year"))
                .whenever(privateHolidayPeriodCreateUseCase)
                .create(eq(createVacationRequestDTO(today, today.plusDays(5))), any())

            assertThrows<MaxNextYearRequestVacationException> {
                vacationController.createPrivateHolidayPeriod(createVacationRequestDTO, ENGLISH)
            }
            verify(privateHolidayPeriodCreateUseCase, times(1)).create(createVacationRequestDTO, ENGLISH)
        }

        @Test
        fun `FAIL to create when the start date closed`() {

            val createVacationRequestDTO = createVacationRequestDTO(today.minusYears(2), tomorrow.minusYears(2))

            doThrow(VacationRangeClosedException())
                .whenever(privateHolidayPeriodCreateUseCase)
                .create(eq(createVacationRequestDTO(today.minusYears(2), tomorrow.minusYears(2))), any())

            assertThrows<VacationRangeClosedException> {
                vacationController.createPrivateHolidayPeriod(createVacationRequestDTO, ENGLISH)
            }
            verify(privateHolidayPeriodCreateUseCase, times(1)).create(createVacationRequestDTO, ENGLISH)
        }

        @Test
        fun `FAIL to create when the start date is before user hiring date`() {

            val createVacationRequestDTO = createVacationRequestDTO(today, tomorrow)

            doThrow(VacationBeforeHiringDateException())
                .whenever(privateHolidayPeriodCreateUseCase)
                .create(eq(createVacationRequestDTO(today, tomorrow)), any())

            assertThrows<VacationBeforeHiringDateException> {
                vacationController.createPrivateHolidayPeriod(createVacationRequestDTO, ENGLISH)
            }
            verify(privateHolidayPeriodCreateUseCase, times(1)).create(createVacationRequestDTO, ENGLISH)
        }

        @Test
        fun `FAIL to create when vacation request overlap with other vacation`() {

            val createVacationRequestDTO = createVacationRequestDTO(today, tomorrow)

            doThrow(VacationRequestOverlapsException())
                .whenever(privateHolidayPeriodCreateUseCase)
                .create(eq(createVacationRequestDTO(today, tomorrow)), any())

            assertThrows<VacationRequestOverlapsException> {
                vacationController.createPrivateHolidayPeriod(createVacationRequestDTO, ENGLISH)
            }
            verify(privateHolidayPeriodCreateUseCase, times(1)).create(createVacationRequestDTO, ENGLISH)
        }

        @Test
        fun `FAIL to create when vacation request is empty`() {

            val createVacationRequestDTO = createVacationRequestDTO(today, today)

            doThrow(VacationRequestEmptyException())
                .whenever(privateHolidayPeriodCreateUseCase)
                .create(eq(createVacationRequestDTO(today, today)), any())

            assertThrows<VacationRequestEmptyException> {
                vacationController.createPrivateHolidayPeriod(createVacationRequestDTO, ENGLISH)
            }
            verify(privateHolidayPeriodCreateUseCase, times(1)).create(createVacationRequestDTO, ENGLISH)
        }
    }

    @Nested
    inner class UpdateVacationPeriod {
        @Test
        fun `update an existing vacation period`() {

            val requestVacationDto = createVacationUpdateDTO(today, tomorrow)
            val createVacationResponseDTO = CreateVacationResponseDTO(
                startDate = today,
                endDate = tomorrow,
                days = 1,
                chargeYear = chargeThisYear
            )

            doReturn(listOf(createVacationResponseDTO))
                .whenever(privateHolidayPeriodUpdateUseCase).update(eq(requestVacationDto), any())

            val updateVacationResponse = vacationController.updatePrivateHolidayPeriod(requestVacationDto, ENGLISH)

            assertEquals(listOf(createVacationResponseDTO), updateVacationResponse)

        }

        @Test
        fun `FAIL to update when the startDate is LATER than end date`() {

            doThrow(DateRangeException(startDate = tomorrow, endDate = today))
                .whenever(privateHolidayPeriodUpdateUseCase)
                .update(eq(createVacationUpdateDTO(startDate = tomorrow, endDate = today)), any())

            assertThrows<DateRangeException> {
                vacationController.updatePrivateHolidayPeriod(
                    createVacationUpdateDTO(startDate = tomorrow, endDate = today),
                    ENGLISH
                )
            }
        }

        @Test
        fun `FAIL when user the vacation period does not belong to the logged user`() {

            doThrow(UserPermissionException("You don't have permission to access the resource"))
                .whenever(privateHolidayPeriodUpdateUseCase)
                .update(eq(createVacationUpdateDTO(today, tomorrow)), any())

            assertThrows<UserPermissionException> {
                vacationController.updatePrivateHolidayPeriod(
                    createVacationUpdateDTO(today, tomorrow),
                    ENGLISH
                )
            }

        }

        @Test
        fun `FAIL when the vacation period is already accepted`() {

            doThrow(VacationAcceptedStateException("The vacation period is accepted"))
                .whenever(privateHolidayPeriodUpdateUseCase)
                .update(eq(createVacationUpdateDTO(today, tomorrow)), any())

            assertThrows<VacationAcceptedStateException> {
                vacationController.updatePrivateHolidayPeriod(
                    createVacationUpdateDTO(today, tomorrow),
                    ENGLISH
                )
            }

        }

        @Test
        fun `FAIL when the vacation period to update is not found in the database`() {

            val id = 20L

            doThrow(VacationNotFoundException(id))
                .whenever(privateHolidayPeriodUpdateUseCase)
                .update(eq(createVacationUpdateDTO(id, today, tomorrow)), any())

            assertThrows<VacationNotFoundException> {
                vacationController.updatePrivateHolidayPeriod(
                    createVacationUpdateDTO(today, tomorrow),
                    ENGLISH
                )
            }
        }

        @Test
        fun `FAIL to update when vacation range is closed`() {

            doThrow(VacationRangeClosedException())
                .whenever(privateHolidayPeriodUpdateUseCase)
                .update(eq(createVacationUpdateDTO(today.minusYears(2), tomorrow.minusYears(2))), any())

            assertThrows<VacationRangeClosedException> {
                vacationController.updatePrivateHolidayPeriod(
                    createVacationUpdateDTO(today.minusYears(2), tomorrow.minusYears(2)),
                    ENGLISH
                )
            }

        }

        @Test
        fun `FAIL to update when vacation start date is before user hiring date`() {

            doThrow(VacationBeforeHiringDateException())
                .whenever(privateHolidayPeriodUpdateUseCase)
                .update(eq(createVacationUpdateDTO(today, tomorrow)), any())

            assertThrows<VacationBeforeHiringDateException> {
                vacationController.updatePrivateHolidayPeriod(
                    createVacationUpdateDTO(today, tomorrow),
                    ENGLISH
                )
            }
        }

        @Test
        fun `FAIL to update when vacation request overlap with other vacation`() {

            doThrow(VacationRequestOverlapsException())
                .whenever(privateHolidayPeriodUpdateUseCase)
                .update(eq(createVacationUpdateDTO(today, tomorrow)), any())

            assertThrows<VacationRequestOverlapsException> {
                vacationController.updatePrivateHolidayPeriod(
                    createVacationUpdateDTO(today, tomorrow),
                    ENGLISH
                )
            }

        }

        @Test
        fun `FAIL to update when vacation request is empty`() {

            doThrow(VacationRequestEmptyException())
                .whenever(privateHolidayPeriodUpdateUseCase)
                .update(eq(createVacationUpdateDTO(today, today)), any())

            assertThrows<VacationRequestEmptyException> {
                vacationController.updatePrivateHolidayPeriod(
                    createVacationUpdateDTO(today, today),
                    ENGLISH
                )
            }

        }


    }

    @Nested
    inner class DeleteVacationPeriod {
        @Test
        fun `delete a vacation period`() {
            val id = 10L

            willDoNothing().given(privateHolidayPeriodDeleteUseCase).delete(id)

            assertDoesNotThrow { vacationController.deletePrivateHolidayPeriod(id) }

        }

        @Test
        fun `FAIL when user the vacation period does not belong to the logged user`() {
            val id = 10L

            doThrow(UserPermissionException())
                .whenever(privateHolidayPeriodDeleteUseCase)
                .delete(id)

            assertThrows<UserPermissionException> {
                vacationController.deletePrivateHolidayPeriod(id)
            }
        }

        @Test
        fun `FAIL when the vacation period is already accepted`() {
            val id = 10L

            doThrow(VacationAcceptedPastPeriodStateException())
                .whenever(privateHolidayPeriodDeleteUseCase)
                .delete(id)

            assertThrows<VacationAcceptedPastPeriodStateException> {
                vacationController.deletePrivateHolidayPeriod(id)
            }
        }

        @Test
        fun `FAIL when the vacation period to delete is not found in the database`() {
            val id = 10L

            doThrow(VacationNotFoundException(id))
                .whenever(privateHolidayPeriodDeleteUseCase)
                .delete(id)

            assertThrows<VacationNotFoundException> {
                vacationController.deletePrivateHolidayPeriod(id)
            }
        }

        @Test
        fun `FAIL to delete when range is closed`() {
            val id = 10L

            doThrow(VacationRangeClosedException())
                .whenever(privateHolidayPeriodDeleteUseCase)
                .delete(id)

            assertThrows<VacationRangeClosedException> {
                vacationController.deletePrivateHolidayPeriod(id)
            }
        }
    }

    private companion object {

        private val today = LocalDate.now()
        private val tomorrow = LocalDate.now().plusDays(1)
        private val chargeThisYear = LocalDate.now().year


        private val HOLIDAYS_DTO = listOf(mock(HolidayDTO::class.java))

        private val VACATIONS_DTO = listOf(
            VacationDTO(
                id = 1,
                state = VacationState.REJECT,
                startDate = LocalDate.of(2020, Month.JANUARY, 1),
                endDate = LocalDate.of(2020, Month.JANUARY, 1),
                days = listOf(LocalDate.of(2020, Month.JANUARY, 1)),
                chargeYear = LocalDate.of(2020, Month.JANUARY, 1)
            ),
            VacationDTO(
                id = 2,
                state = VacationState.CANCELLED,
                startDate = LocalDate.of(2020, Month.JANUARY, 2),
                endDate = LocalDate.of(2020, Month.JANUARY, 2),
                days = listOf(LocalDate.of(2020, Month.JANUARY, 2)),
                chargeYear = LocalDate.of(2020, Month.JANUARY, 1)
            ),
            VacationDTO(
                id = 3,
                state = ACCEPT,
                startDate = LocalDate.of(2020, Month.JANUARY, 3),
                endDate = LocalDate.of(2020, Month.JANUARY, 6),
                days = listOf(LocalDate.of(2020, Month.JANUARY, 4), LocalDate.of(2020, Month.JANUARY, 5)),
                chargeYear = LocalDate.of(2020, Month.JANUARY, 1)
            ),
            VacationDTO(
                id = 4,
                state = PENDING,
                startDate = LocalDate.of(2020, Month.JANUARY, 7),
                endDate = LocalDate.of(2020, Month.JANUARY, 7),
                days = listOf(LocalDate.of(2020, Month.JANUARY, 7)),
                chargeYear = LocalDate.of(2020, Month.JANUARY, 1)
            )
        )

    }


}
