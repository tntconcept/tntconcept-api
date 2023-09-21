package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.entities.Vacation
import com.autentia.tnt.binnacle.entities.VacationState
import com.autentia.tnt.binnacle.entities.dto.AbsenceDTO
import com.autentia.tnt.binnacle.entities.dto.AbsenceFilterDTO
import com.autentia.tnt.binnacle.entities.dto.AbsenceType
import com.autentia.tnt.binnacle.repositories.VacationRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.Month

internal class AbsencesByFilterUseCaseTest {

    private val vacationRepository = mock<VacationRepository>()
    private val absencesByFilterUseCase = AbsencesByFilterUseCase(vacationRepository)


    @Test
    fun `should return all the vacations by date range`() {
        val startDate = LocalDate.of(2023, Month.SEPTEMBER, 1)
        val endDate = LocalDate.of(2023, Month.SEPTEMBER, 30)

        val absenceFilterDTO = AbsenceFilterDTO(startDate, endDate)
        val expectedAbsences = listOf(ABSENCE_01)

        whenever(vacationRepository.find(startDate, endDate)).doReturn(listOf(VACATION_01))

        val absences = absencesByFilterUseCase.getAbsences(absenceFilterDTO)


        assertEquals(expectedAbsences, absences)

    }

    private companion object {
        private val START_DATE_01 = LocalDate.of(2023, Month.SEPTEMBER, 5)
        private val END_DATE_01 = LocalDate.of(2023, Month.SEPTEMBER, 10)
        private val VACATION_01 = Vacation(1, START_DATE_01, END_DATE_01, VacationState.PENDING, 2, "observations", null, "vacations", LocalDate.of(2023,Month.JANUARY,1))
        private val ABSENCE_01 = AbsenceDTO(2, "John Doe", AbsenceType.VACATION, START_DATE_01, END_DATE_01 )

    }
}