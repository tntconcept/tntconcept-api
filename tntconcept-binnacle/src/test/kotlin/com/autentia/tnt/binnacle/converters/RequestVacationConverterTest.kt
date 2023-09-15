package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.entities.dto.RequestVacationDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class RequestVacationConverterTest {
    private lateinit var sut: RequestVacationConverter

    @BeforeEach
    fun setUp() {
        sut = RequestVacationConverter()
    }

    @Test
    fun toRequestVacation() {
        val requestVacationDTO = RequestVacationDTO(
            1,
            LocalDate.of(2020, 12, 21),
            LocalDate.of(2020, 12, 23),
            2020,
            "Description..."
        )

        val requestVacation = sut.toRequestVacation(requestVacationDTO)

        assertEquals(requestVacationDTO.id, requestVacation.id)
        assertEquals(requestVacationDTO.startDate, requestVacation.startDate)
        assertEquals(requestVacationDTO.endDate, requestVacation.endDate)
        assertEquals(requestVacationDTO.chargeYear, requestVacation.chargeYear)
        assertEquals(requestVacationDTO.description, requestVacation.description)
    }
}
