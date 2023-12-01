package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.Vacation
import com.autentia.tnt.binnacle.entities.VacationState
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.time.LocalDate
import java.time.Month

@MicronautTest
@TestInstance(PER_CLASS)
internal class VacationDaoIT {

    @Inject
    private lateinit var vacationDao: VacationDao

    private val userId = 1L
    private val christmas2020 = Vacation(
        id = null,
        startDate = LocalDate.of(2020, Month.DECEMBER, 24),
        endDate = LocalDate.of(2020, Month.DECEMBER, 31),
        state = VacationState.ACCEPT,
        chargeYear = LocalDate.of(2020, Month.JANUARY, 1),
        userId = userId,
        description = ""
    )
    private val reyes2021 = Vacation(
        id = null,
        startDate = LocalDate.of(2021, Month.JANUARY, 6),
        endDate = LocalDate.of(2021, Month.JANUARY, 10),
        state = VacationState.ACCEPT,
        chargeYear = LocalDate.of(2021, Month.JANUARY, 1),
        userId = userId,
        description = ""
    )
    private val carnaval2021 = Vacation(
        id = null,
        startDate = LocalDate.of(2021, Month.FEBRUARY, 15),
        endDate = LocalDate.of(2021, Month.FEBRUARY, 18),
        state = VacationState.ACCEPT,
        chargeYear = LocalDate.of(2021, Month.JANUARY, 1),
        userId = userId,
        description = ""
    )
    private val christmas2021 = Vacation(
        id = null,
        startDate = LocalDate.of(2021, Month.DECEMBER, 24),
        endDate = LocalDate.of(2021, Month.DECEMBER, 31),
        state = VacationState.ACCEPT,
        chargeYear = LocalDate.of(2021, Month.JANUARY, 1),
        userId = userId,
        description = ""
    )

    private val sanjose2021 = Vacation(
        id = null,
        startDate = LocalDate.of(2021, Month.MARCH, 19),
        endDate = LocalDate.of(2021, Month.MARCH, 19),
        state = VacationState.PENDING,
        chargeYear = LocalDate.of(2021, Month.JANUARY, 1),
        userId = userId,
        description = ""
    )

    private val summer2021 = Vacation(
        id = null,
        startDate = LocalDate.of(2021, Month.JULY, 15),
        endDate = LocalDate.of(2021, Month.JULY, 20),
        state = VacationState.CANCELLED,
        chargeYear = LocalDate.of(2021, Month.JANUARY, 1),
        userId = userId,
        description = ""
    )

    private val spring2021 = Vacation(
        id = null,
        startDate = LocalDate.of(2021, Month.APRIL, 15),
        endDate = LocalDate.of(2021, Month.APRIL, 20),
        state = VacationState.REJECT,
        chargeYear = LocalDate.of(2021, Month.JANUARY, 1),
        userId = userId,
        description = ""
    )

    @BeforeEach
    fun setUpTest() {
        vacationDao.deleteAll()
    }

    @Test
    fun `filter user vacations between start and end date`() {
        val vacations = listOf(
            christmas2020.copy(),
            reyes2021.copy(),
            carnaval2021.copy()
        )
        vacationDao.saveAll(vacations)

        val startDate = LocalDate.of(2020, Month.JANUARY, 1)
        val endDate = LocalDate.of(2021, Month.DECEMBER, 31)

        val actual = vacationDao.find(startDate, endDate, userId)

        assertEquals(vacations.size, actual.size)
        assertTrue(actual.containsAll(vacations))
    }

    @Test
    fun `filter user vacations between start and end date in the same year`() {
        val vacations = listOf(
            christmas2020.copy(),
            reyes2021.copy(),
            carnaval2021.copy()
        )
        vacationDao.saveAll(vacations)

        val startDate = LocalDate.of(2021, Month.JANUARY, 1)
        val endDate = LocalDate.of(2021, Month.DECEMBER, 31)

        val actual = vacationDao.find(startDate, endDate, userId)

        assertEquals(2, actual.size)
        assertTrue(actual.containsAll(listOf(vacations[1], vacations[2])))
    }

    @Test
    fun `filter user vacations between charge year`() {
        val vacations = listOf(
            christmas2020.copy(),
            reyes2021.copy(),
            carnaval2021.copy(),
            christmas2021.copy()
        )
        vacationDao.saveAll(vacations)

        val startYear = LocalDate.of(2021, Month.JANUARY, 1)
        val endYear = LocalDate.of(2021, Month.JANUARY, 1)

        val actual = vacationDao.findBetweenChargeYears(startYear, endYear, userId)

        assertEquals(3, actual.size)
        assertTrue(actual.containsAll(listOf(vacations[1], vacations[2], vacations[3])))
    }

    @Test
    fun `filter user vacations by dates and states`() {
        val vacations = listOf(
            christmas2021.copy(),
            sanjose2021.copy(),
            summer2021.copy(),
            spring2021.copy()
        )
        vacationDao.saveAll(vacations)

        val startDate = LocalDate.of(2021, Month.JANUARY, 1)
        val endDate = LocalDate.of(2021, Month.DECEMBER, 31)

        val actual = vacationDao.findByDatesAndStates(startDate, endDate, listOf(VacationState.PENDING,VacationState.ACCEPT))

        assertEquals(2, actual.size)
        assertTrue(actual.containsAll(listOf(vacations[0], vacations[1])))
    }
}
