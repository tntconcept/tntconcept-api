package com.autentia.tnt.api.binnacle.calendar

import com.autentia.tnt.api.binnacle.exchangeObject
import com.autentia.tnt.api.binnacle.toJson
import com.autentia.tnt.binnacle.usecases.CalendarDaysForProjectRoleUseCase
import com.autentia.tnt.binnacle.usecases.CalendarWorkableDaysUseCase
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
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CalendarControllerIT {

    @get:MockBean(CalendarWorkableDaysUseCase::class)
    internal val calendarWorkableDaysUseCase = mock<CalendarWorkableDaysUseCase>()

    @get:MockBean(CalendarDaysForProjectRoleUseCase::class)
    internal val calendarDaysForProjectRoleUseCase = mock<CalendarDaysForProjectRoleUseCase>()

    @Inject
    @field:Client("/")
    private lateinit var httpClient: HttpClient

    private lateinit var client: BlockingHttpClient

    @BeforeAll
    fun setup() {
        client = httpClient.toBlocking()
    }

    @Test
    fun `get workable days`() {
        val startDate = LocalDate.of(2023, 1, 10)
        val endDate = LocalDate.of(2023, 1, 20)

        whenever(calendarWorkableDaysUseCase.get(startDate, endDate)).thenReturn(3)

        val response = client.exchangeObject<Int>(
            HttpRequest.GET("/api/calendar/workable-days/count?startDate=${startDate.toJson()}&endDate=${endDate.toJson()}")
        )

        assertEquals(HttpStatus.OK, response.status)
        assertEquals(3, response.body.get())
    }

    @Test
    fun `get days between dates for project role`() {
        val startDate = LocalDate.of(2023, 1, 10)
        val endDate = LocalDate.of(2023, 1, 20)
        val roleId = 1L

        whenever(calendarDaysForProjectRoleUseCase.get(startDate, endDate, roleId)).thenReturn(3)

        val response = client.exchangeObject<Int>(
            HttpRequest.GET("/api/calendar/days/count?startDate=${startDate.toJson()}&endDate=${endDate.toJson()}&roleId=${roleId}")
        )

        assertEquals(HttpStatus.OK, response.status)
        assertEquals(3, response.body.get())
    }
}