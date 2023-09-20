package com.autentia.tnt.api.binnacle.holiday

import com.autentia.tnt.api.binnacle.exchangeList
import com.autentia.tnt.api.binnacle.vacation.HolidayDetailsResponse
import com.autentia.tnt.binnacle.entities.dto.HolidayDTO
import com.autentia.tnt.binnacle.usecases.UserHolidayBetweenDatesUseCase
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
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

    @get:MockBean(UserHolidayBetweenDatesUseCase::class)
    internal val holidayBetweenDateForUserUseCase = mock<UserHolidayBetweenDatesUseCase>()


    @BeforeAll
    fun setUp() {
        client = httpClient.toBlocking()
    }

    @Test
    fun `get the holidays by year`() {
        val currYear = LocalDate.now().year
        doReturn(HOLIDAYS_RESPONSE_DTO).whenever(holidayBetweenDateForUserUseCase).getHolidays(currYear)

        val response = client.exchangeList<HolidayDetailsResponse>(
            HttpRequest.GET("/api/holiday?year=${currYear}")
        )

        assertEquals(HttpStatus.OK, response.status())
        assertEquals(HOLIDAY_RESPONSE, response.body.get())
    }

    @Test
    fun `get the holidays of current year if no year provided`() {
        doReturn(HOLIDAYS_RESPONSE_DTO).whenever(holidayBetweenDateForUserUseCase).getHolidays(null)

        val response = client.exchangeList<HolidayDetailsResponse>(
            HttpRequest.GET("/api/holiday")
        )

        assertEquals(HttpStatus.OK, response.status())
        assertEquals(HOLIDAY_RESPONSE, response.body.get())
    }

    @Test
    fun `get the an empty array when year has no holidays`() {
        val currentYear = 2022
        doReturn(listOf<HolidayDTO>()).whenever(holidayBetweenDateForUserUseCase).getHolidays(currentYear)

        val response = client.exchangeList<HolidayDetailsResponse>(
            HttpRequest.GET("/api/holiday?year=${currentYear}")
        )

        assertEquals(HttpStatus.OK, response.status())
        assertEquals(listOf<HolidayDetailsResponse>(), response.body.get())
    }

    private companion object {

        private val HOLIDAYS_RESPONSE_DTO =
            listOf(
                HolidayDTO(1, "New year", LocalDate.of(LocalDate.now().year, 1, 1)),
                HolidayDTO(2, "Santa", LocalDate.of(LocalDate.now().year, 12, 25))
            )


        private val HOLIDAY_RESPONSE =
            listOf(
                HolidayDetailsResponse(1, "New year", LocalDate.of(LocalDate.now().year, 1, 1)),
                HolidayDetailsResponse(2, "Santa", LocalDate.of(LocalDate.now().year, 12, 25))
            )
    }

}