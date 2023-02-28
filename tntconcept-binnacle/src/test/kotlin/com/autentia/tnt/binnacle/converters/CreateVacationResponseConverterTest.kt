package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.CreateVacationResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class CreateVacationResponseConverterTest{

    private lateinit var sut: CreateVacationResponseConverter

    @BeforeEach
    fun setUp(){
        sut = CreateVacationResponseConverter()
    }

    @Test
    fun `given CreateVacationResponse should return domain CreateVacationResponseDTO`() {
        val createVacationResponse = CreateVacationResponse(
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(1),
            days = 1,
            chargeYear = LocalDate.now().year
        )

        val createVacationResponseDTO = sut.toCreateVacationResponseDTO(createVacationResponse)

        assertEquals(createVacationResponse.startDate, createVacationResponseDTO.startDate)
        assertEquals(createVacationResponse.endDate, createVacationResponseDTO.endDate)
        assertEquals(createVacationResponse.days, createVacationResponseDTO.days)
        assertEquals(createVacationResponse.chargeYear, createVacationResponseDTO.chargeYear)
    }

    @Test
    fun `given CreateVacationResponse list should return CreateVacationResponseDTO list`() {
        //Given
        val listCreateVacationResponse = listOf(
            CreateVacationResponse(
                startDate = LocalDate.now(),
                endDate = LocalDate.now().plusDays(1),
                days = 1,
                chargeYear = LocalDate.now().year
            )
        ).toMutableList()

        //When
        val listCreateVacationResponseDTO = listCreateVacationResponse.map {  sut.toCreateVacationResponseDTO(it) }

        //Then
        assertThat(listCreateVacationResponseDTO).hasSize(1)

    }
}
