package com.autentia.tnt.api.binnacle.holiday

import com.autentia.tnt.api.binnacle.exchangeObject
import com.autentia.tnt.api.binnacle.vacation.HolidayDetailsResponse
import com.autentia.tnt.api.binnacle.vacation.HolidayResponse
import com.autentia.tnt.api.binnacle.vacation.VacationControllerIT
import com.autentia.tnt.api.binnacle.vacation.VacationResponse
import com.autentia.tnt.binnacle.entities.VacationState
import com.autentia.tnt.binnacle.entities.dto.HolidayDTO
import com.autentia.tnt.binnacle.entities.dto.HolidayResponseDTO
import com.autentia.tnt.binnacle.entities.dto.VacationDTO
import com.autentia.tnt.binnacle.usecases.HolidaysBetweenDateForAuthenticateUserUseCase
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class HolidayControllerIT {

    @Inject
    @field:Client("/")
    private lateinit var httpClient: HttpClient

    private lateinit var client: BlockingHttpClient

    @get:MockBean(HolidaysBetweenDateForAuthenticateUserUseCase::class)
    internal val holidaysBetweenDateForUserUseCase = mock<HolidaysBetweenDateForAuthenticateUserUseCase>()


    @BeforeAll
    fun setUp() {
        client = httpClient.toBlocking()
    }

    @Test
    fun `get the vacations by charge year`() {
        doReturn(HOLIDAY_RESPONSE_DTO).whenever(holidaysBetweenDateForUserUseCase).getHolidays(
            START_DATE, START_DATE.plusDays(3L)
        )

        val response = client.exchangeObject<HolidayResponse>(
            HttpRequest.GET("/api/vacations?chargeYear=${START_DATE.year}")
        )

        assertEquals(HttpStatus.OK, response.status())
        assertEquals(HOLIDAY_RESPONSE, response.body.get())
    }

    private companion object {
        private val START_DATE = LocalDate.of(2023, 7, 6)

        val VACATION_DTO = VacationDTO(
            2,
            "Observations",
            "Description",
            VacationState.PENDING,
            START_DATE,
            START_DATE.plusDays(1L),
            listOf(START_DATE),
            START_DATE
        )
        val VACATION_RESPONSE = VacationResponse(
            2,
            "Observations",
            "Description",
            VacationState.PENDING,
            START_DATE,
            START_DATE.plusDays(1L),
            listOf(START_DATE),
            START_DATE
        )

        private val HOLIDAY_RESPONSE_DTO = HolidayResponseDTO(
            listOf(HolidayDTO(1, "New year", LocalDate.of(LocalDate.now().year, 1, 1))),
            listOf(VACATION_DTO)
        )

        private val HOLIDAY_RESPONSE = HolidayResponse(
            listOf(HolidayDetailsResponse(1, "New year", LocalDate.of(LocalDate.now().year, 1, 1))),
            listOf(VACATION_RESPONSE)
        )
    }

}