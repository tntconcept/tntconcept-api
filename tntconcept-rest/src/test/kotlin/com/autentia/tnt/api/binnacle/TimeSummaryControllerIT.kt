package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.entities.dto.AnnualBalanceDTO
import com.autentia.tnt.binnacle.entities.dto.MonthlyBalanceDTO
import com.autentia.tnt.binnacle.entities.dto.MonthlyRolesDTO
import com.autentia.tnt.binnacle.entities.dto.PreviousAnnualBalanceDTO
import com.autentia.tnt.binnacle.entities.dto.TimeSummaryDTO
import com.autentia.tnt.binnacle.entities.dto.YearAnnualBalanceDTO
import com.autentia.tnt.binnacle.usecases.UserTimeSummaryUseCase
import io.micronaut.http.HttpRequest.GET
import io.micronaut.http.HttpStatus
import io.micronaut.http.HttpStatus.BAD_REQUEST
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
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.Month

@MicronautTest
@TestInstance(PER_CLASS)
internal class TimeSummaryControllerIT {

    @Inject
    @field:Client("/")
    private lateinit var httpClient: HttpClient

    private lateinit var client: BlockingHttpClient

    @get:MockBean(UserTimeSummaryUseCase::class)
    internal val userTimeSummaryUseCase = mock<UserTimeSummaryUseCase>()

    @BeforeAll
    fun setUp() {
        client = httpClient.toBlocking()
    }

    @Test
    fun `get working time with a valid date`() {
        val date = LocalDate.of(2020, Month.FEBRUARY, 15)
        val roles = listOf<MonthlyRolesDTO>()

        val expectedTimeSummary = TimeSummaryDTO(
            YearAnnualBalanceDTO(
                createPreviousAnnualBalance(0.00, 1765.00, -1765.00),
                createAnnualBalance(0.00, 1765.00, 176.00, -1941.00)
            ),
            listOf(createMonthlyBalance(184.0, 0.00, 154.93, -154.93, roles, 0.00))
        )
        doReturn(expectedTimeSummary).whenever(userTimeSummaryUseCase).getTimeSummary(date)

        val response = client.exchangeObject<TimeSummaryDTO>(
            GET("/api/time-summary?date=${date.toJson()}")
        )

        assertEquals(HttpStatus.OK.code, response.status.code)
        assertEquals(expectedTimeSummary, response.body.get())
    }

    @Test
    fun `fail getting working time with a wrong date`() {
        val malformedDate = "xxxx-02-01"

        val ex = assertThrows<HttpClientResponseException> {
            client.exchangeObject<Any>(
                GET("/api/time-summary?date=$malformedDate")
            )
        }

        assertEquals(BAD_REQUEST, ex.status)
    }

    private fun createAnnualBalance(worked: Double, target: Double, notRequested: Double, balance: Double) =
        AnnualBalanceDTO(
            worked.toBigDecimal(),
            target.toBigDecimal(),
            notRequested.toBigDecimal(),
            balance.toBigDecimal()
        )

    private fun createPreviousAnnualBalance(worked: Double, target: Double, balance: Double) =
        PreviousAnnualBalanceDTO(
            worked.toBigDecimal(),
            target.toBigDecimal(),
            balance.toBigDecimal()
        )

    private fun createMonthlyBalance(
        workable: Double,
        worked: Double,
        recommended: Double,
        balance: Double,
        roles: List<MonthlyRolesDTO>,
        vacation: Double
    ) =
        MonthlyBalanceDTO(
            workable.toBigDecimal(),
            worked.toBigDecimal(),
            recommended.toBigDecimal(),
            balance.toBigDecimal(),
            roles,
            vacation.toBigDecimal()
        )

}
