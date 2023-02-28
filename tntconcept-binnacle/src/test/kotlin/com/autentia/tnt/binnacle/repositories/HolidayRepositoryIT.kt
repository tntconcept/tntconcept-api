package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.Holiday
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDateTime

@MicronautTest
@TestInstance(PER_CLASS)
internal class HolidayRepositoryIT {

    @Inject
    private lateinit var holidayRepository: HolidayRepository

    private val newYear = Holiday(1, "Año nuevo", LocalDateTime.of(2019, 1, 1, 0, 0, 0))
    private val reyes = Holiday(2, "Reyes", LocalDateTime.of(2019, 1, 7, 0, 0, 0))
    private val semanaSanta = Holiday(3, "Semana Santa", LocalDateTime.of(2019, 3, 18, 0, 0, 0))

    @BeforeAll
    internal fun setUp() {
        holidayRepository.saveAll(listOf(newYear, reyes, semanaSanta))
    }

    private fun holidaysProvider() = arrayOf(
        arrayOf(LocalDateTime.of(2019, 2, 1, 0, 0), LocalDateTime.of(2019, 2, 28, 0, 0), emptyList<Holiday>()),
        arrayOf(
            LocalDateTime.of(2019, 1, 1, 0, 0),
            LocalDateTime.of(2019, 1, 31, 0, 0),
            listOf(newYear, reyes)
        ),
        arrayOf(
            LocalDateTime.of(2019, 1, 1, 0, 0),
            LocalDateTime.of(2019, 3, 20, 0, 0),
            listOf(newYear, reyes, semanaSanta)
        )
    )

    @ParameterizedTest
    @MethodSource("holidaysProvider")
    fun `returns holidays between two dates`(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        expectedHolidays: List<Holiday>
    ) {

        val result = holidayRepository.findAllByDateBetween(startDate, endDate)

        assertEquals(expectedHolidays, result)
    }

}
