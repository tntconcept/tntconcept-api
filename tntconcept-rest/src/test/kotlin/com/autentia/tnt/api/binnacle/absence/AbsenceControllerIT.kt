package com.autentia.tnt.api.binnacle.absence

import com.autentia.tnt.api.binnacle.exchangeList
import com.autentia.tnt.api.binnacle.toJson
import com.autentia.tnt.binnacle.entities.dto.AbsenceDTO
import com.autentia.tnt.binnacle.entities.dto.AbsenceFilterDTO
import com.autentia.tnt.binnacle.entities.dto.AbsenceType
import com.autentia.tnt.binnacle.usecases.AbsencesByFilterUseCase
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
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.Month

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class AbsenceControllerIT {
    @Inject
    @field:Client(value = "/", errorType = String::class)
    private lateinit var httpClient: HttpClient
    private lateinit var client: BlockingHttpClient

    @get:MockBean(AbsencesByFilterUseCase::class)
    internal val absenceByFilterUseCase = mock<AbsencesByFilterUseCase>()

    @BeforeAll
    fun setup() {
        client = httpClient.toBlocking()
    }

    @Test
    fun `filter absences by date range`() {
        val startDate = LocalDate.of(2023, Month.SEPTEMBER, 1)
        val endDate = LocalDate.of(2023, Month.SEPTEMBER, 30)
        val absenceFilterDto = AbsenceFilterDTO(startDate, endDate)

        doReturn(listOf(ABSENCE_DTO)).whenever(absenceByFilterUseCase).getAbsences(absenceFilterDto)

        val response = client.exchangeList<AbsenceResponse>(
            HttpRequest.GET("/api/absence?startDate=${startDate.toJson()}&endDate=${endDate.toJson()}"),
        )

        assertEquals(HttpStatus.OK, response.status)
        assertEquals(listOf(AbsenceResponse.from(ABSENCE_DTO)), response.body.get())
    }

    @Test
    fun `filter absences by date range and organizations`() {
        val startDate = LocalDate.of(2023, Month.SEPTEMBER, 1)
        val endDate = LocalDate.of(2023, Month.SEPTEMBER, 30)
        val absenceFilterDto = AbsenceFilterDTO(startDate, endDate, null, listOf(1L,2L), null)

        doReturn(listOf(ABSENCE_DTO)).whenever(absenceByFilterUseCase).getAbsences(absenceFilterDto)

        val response = client.exchangeList<AbsenceResponse>(
            HttpRequest.GET("/api/absence?startDate=${startDate.toJson()}&endDate=${endDate.toJson()}&organizationIds=1,2"),
        )

        assertEquals(HttpStatus.OK, response.status)
        assertEquals(listOf(AbsenceResponse.from(ABSENCE_DTO)), response.body.get())
    }

    @Test
    fun `filter absences by date range and projects`() {
        val startDate = LocalDate.of(2023, Month.SEPTEMBER, 1)
        val endDate = LocalDate.of(2023, Month.SEPTEMBER, 30)
        val absenceFilterDto = AbsenceFilterDTO(startDate, endDate, null, null, listOf(1L,2L))

        doReturn(listOf(ABSENCE_DTO)).whenever(absenceByFilterUseCase).getAbsences(absenceFilterDto)

        val response = client.exchangeList<AbsenceResponse>(
            HttpRequest.GET("/api/absence?startDate=${startDate.toJson()}&endDate=${endDate.toJson()}&projectIds=1,2"),
        )

        assertEquals(HttpStatus.OK, response.status)
        assertEquals(listOf(AbsenceResponse.from(ABSENCE_DTO)), response.body.get())
    }

    @Test
    fun `filter absences by date range and users`() {
        val startDate = LocalDate.of(2023, Month.SEPTEMBER, 1)
        val endDate = LocalDate.of(2023, Month.SEPTEMBER, 30)
        val absenceFilterDto = AbsenceFilterDTO(startDate, endDate, listOf(1L,2L), null, null)

        doReturn(listOf(ABSENCE_DTO)).whenever(absenceByFilterUseCase).getAbsences(absenceFilterDto)

        val response = client.exchangeList<AbsenceResponse>(
            HttpRequest.GET("/api/absence?startDate=${startDate.toJson()}&endDate=${endDate.toJson()}&userIds=1,2"),
        )

        assertEquals(HttpStatus.OK, response.status)
        assertEquals(listOf(AbsenceResponse.from(ABSENCE_DTO)), response.body.get())
    }

    @Test
    fun `filter absences by all filters`() {
        val startDate = LocalDate.of(2023, Month.SEPTEMBER, 1)
        val endDate = LocalDate.of(2023, Month.SEPTEMBER, 30)
        val absenceFilterDto = AbsenceFilterDTO(startDate, endDate, listOf(1L,2L), listOf(13L,52L), listOf(3L, 5L))

        doReturn(listOf(ABSENCE_DTO)).whenever(absenceByFilterUseCase).getAbsences(absenceFilterDto)

        val response = client.exchangeList<AbsenceResponse>(
            HttpRequest.GET("/api/absence?startDate=${startDate.toJson()}&endDate=${endDate.toJson()}&userIds=1,2&organizationIds=13,52&projectIds=3,5"),
        )

        assertEquals(HttpStatus.OK, response.status)
        assertEquals(listOf(AbsenceResponse.from(ABSENCE_DTO)), response.body.get())
    }

    @Test
    fun `returns BAD_REQUEST when invalid list of user ids`() {
        val startDate = LocalDate.of(2023, Month.SEPTEMBER, 1)
        val endDate = LocalDate.of(2023, Month.SEPTEMBER, 30)

        val ex = assertThrows<HttpClientResponseException> {
            client.exchangeList<AbsenceResponse>(
                HttpRequest.GET("/api/absence?startDate=${startDate.toJson()}&endDate=${endDate.toJson()}&userIds=aaaa,12"),
            )
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.status)
    }

    @Test
    fun `returns BAD_REQUEST when date range is not defined`() {
        val ex = assertThrows<HttpClientResponseException> {
            client.exchangeList<AbsenceResponse>(
                HttpRequest.GET("/api/absence?userIds=11,12"),
            )
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.status)
    }

    private companion object {

        private val ABSENCE_DTO = AbsenceDTO(11, "John Doe", AbsenceType.VACATION, LocalDate.of(2023,Month.SEPTEMBER, 5),LocalDate.of(2023,Month.SEPTEMBER, 5) )

    }
}