package com.autentia.tnt.api.binnacle.vacation

import com.autentia.tnt.api.binnacle.createVacationDTO
import com.autentia.tnt.api.binnacle.createVacationResponse
import com.autentia.tnt.api.binnacle.exchangeList
import com.autentia.tnt.api.binnacle.exchangeObject
import com.autentia.tnt.binnacle.usecases.UsersVacationsFromPeriodUseCase
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.Month.DECEMBER
import java.time.Month.JANUARY

@MicronautTest
@TestInstance(PER_CLASS)
internal class VacationControllerIT {

    @Inject
    @field:Client("/")
    private lateinit var httpClient: HttpClient

    private lateinit var client: BlockingHttpClient

    @get:MockBean(UsersVacationsFromPeriodUseCase::class)
    internal val usersVacationsFromPeriodUseCase = mock<UsersVacationsFromPeriodUseCase>()

    @BeforeAll
    fun setUp() {
        client = httpClient.toBlocking()
    }

    fun getPeriodAndStatusProvider() = listOf(
        Arguments.of(
            "malformed startDate and endDate",
            "2023-xx-01",
            "2023-12-xx"
        ),
        Arguments.of(
            "malformed startDate",
            "xxxx-01-01",
            "2023-12-31"
        ),
        Arguments.of(
            "malformed endDate",
            "2023-01-01",
            "xxxx-12-31"
        ),
    )

    @ParameterizedTest(name = "{0}")
    @MethodSource("getPeriodAndStatusProvider")
    fun `return bad request when endDate is wrong formatted`(
        description: String,
        startDate: String,
        endDate: String,
    ) {

        val result = assertThrows<HttpClientResponseException> {
            client.exchangeObject<Any>(
                HttpRequest.GET("/api/vacation?startDate=$startDate&endDate=$endDate")
            )
        }

        assertEquals(HttpStatus.BAD_REQUEST, result.status)
    }

    @Test
    fun `return bad request when startDate is missing`() {

        val result = assertThrows<HttpClientResponseException> {
            client.exchangeObject<Any>(
                HttpRequest.GET("/api/vacation?endDate=2023-01-01")
            )
        }

        assertEquals(HttpStatus.BAD_REQUEST, result.status)
    }

    @Test
    fun `return bad request when endDate is missing`() {

        val result = assertThrows<HttpClientResponseException> {
            client.exchangeObject<Any>(
                HttpRequest.GET("/api/vacation?startDate=2023-01-01")
            )
        }

        assertEquals(HttpStatus.BAD_REQUEST, result.status)
    }

    @Test
    fun `return bad request when startDate and endDate are missing`() {

        val result = assertThrows<HttpClientResponseException> {
            client.exchangeObject<Any>(
                HttpRequest.GET("/api/vacation")
            )
        }

        assertEquals(HttpStatus.BAD_REQUEST, result.status)
    }

    @Test
    fun `get an empty list of users vacations given a period`() {

        doReturn(emptyList<VacationResponse>()).whenever(usersVacationsFromPeriodUseCase).getVacationsByPeriod(
            FIRST_DAY_OF_YEAR,
            LAST_DAY_OF_YEAR
        )

        val response = client.exchangeList<VacationResponse>(
            HttpRequest.GET("/api/vacation?startDate=${FIRST_DAY_OF_YEAR}&endDate=${LAST_DAY_OF_YEAR}&userIds=${USER1}")
        )

        assertEquals(HttpStatus.OK, response.status())
        assertEquals(emptyList<VacationResponse>(), response.body.get())
    }

    @Test
    fun `get a list of users vacations given a period`() {

        doReturn(VACATIONS).whenever(usersVacationsFromPeriodUseCase).getVacationsByPeriod(
            FIRST_DAY_OF_YEAR,
            LAST_DAY_OF_YEAR
        )

        val response = client.exchangeList<VacationResponse>(
            HttpRequest.GET("/api/vacation?startDate=${FIRST_DAY_OF_YEAR}&endDate=${LAST_DAY_OF_YEAR}&userIds=${USER1}")
        )

        assertEquals(HttpStatus.OK, response.status())
        assertEquals(VACATIONS_RESPONSE, response.body.get())
    }

    private companion object {
        private val TODAY = LocalDate.now()
        private val CURRENT_YEAR = TODAY.year

        private val FIRST_DAY_OF_YEAR = LocalDate.of(CURRENT_YEAR, JANUARY, 1)
        private val LAST_DAY_OF_YEAR = LocalDate.of(CURRENT_YEAR, DECEMBER, 31)

        private val USER1 = 1L

        private val VACATIONS = listOf(
            createVacationDTO(
                id = 1L,
                startDate = LocalDate.of(CURRENT_YEAR, 8, 10),
                endDate = LocalDate.of(CURRENT_YEAR, 8, 25)
            ),
            createVacationDTO(
                id = 2L,
                startDate = LocalDate.of(CURRENT_YEAR, 10, 25),
                endDate = LocalDate.of(CURRENT_YEAR, 11, 3)
            )
        )


        private val VACATIONS_RESPONSE = listOf(
            createVacationResponse(
                id = 1L,
                startDate = LocalDate.of(CURRENT_YEAR, 8, 10),
                endDate = LocalDate.of(CURRENT_YEAR, 8, 25)
            ),
            createVacationResponse(
                id = 2L,
                startDate = LocalDate.of(CURRENT_YEAR, 10, 25),
                endDate = LocalDate.of(CURRENT_YEAR, 11, 3)
            )
        )
    }


}