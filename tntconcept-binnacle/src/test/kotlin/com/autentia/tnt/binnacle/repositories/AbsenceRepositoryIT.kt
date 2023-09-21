package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.Absence
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDate.*
import java.time.Month

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AbsenceRepositoryIT {
    @Inject
    private lateinit var absenceRepository: AbsenceRepository

    @Test
    fun `should recover holidays and vacations between two dates` (){
        val startDate = of(2023, Month.JANUARY, 1)
        val endDate = of(2023, Month.DECEMBER, 31)

        val absences = absenceRepository.findAllByDateBetween(startDate, endDate, setOf<Long>(1, 2))

        // TODO preparar datos de prueba y completar assert.
        Assertions.assertEquals(listOf(Absence(1, 1, "asdf", "VACATION", startDate, endDate)), absences)

    }

}