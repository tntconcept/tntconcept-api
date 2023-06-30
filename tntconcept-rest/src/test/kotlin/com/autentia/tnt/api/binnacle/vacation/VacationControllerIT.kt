package com.autentia.tnt.api.binnacle.vacation

import com.autentia.tnt.api.binnacle.ErrorResponse
import com.autentia.tnt.api.binnacle.exchangeList
import com.autentia.tnt.api.binnacle.exchangeObject
import com.autentia.tnt.api.binnacle.getBody
import com.autentia.tnt.api.binnacle.vacation.*
import com.autentia.tnt.binnacle.entities.VacationState
import com.autentia.tnt.binnacle.entities.dto.*
import com.autentia.tnt.binnacle.exception.*
import com.autentia.tnt.binnacle.usecases.*
import io.micronaut.http.HttpHeaders.ACCEPT_LANGUAGE
import io.micronaut.http.HttpRequest.*
import io.micronaut.http.HttpStatus
import io.micronaut.http.HttpStatus.*
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
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit.DAYS
import java.util.*

@MicronautTest
@TestInstance(PER_CLASS)
internal class VacationControllerIT {

    @Inject
    @field:Client("/")
    private lateinit var httpClient: HttpClient

    private lateinit var client: BlockingHttpClient

    @get:MockBean(PrivateHolidaysByChargeYearUseCase::class)
    internal val privateHolidaysByChargeYearUseCase = mock<PrivateHolidaysByChargeYearUseCase>()

    @get:MockBean(PrivateHolidayDetailsUseCase::class)
    internal val privateHolidayDetailsUseCase = mock<PrivateHolidayDetailsUseCase>()

    @get:MockBean(PrivateHolidayPeriodCreateUseCase::class)
    internal val privateHolidayPeriodCreateUseCase = mock<PrivateHolidayPeriodCreateUseCase>()

    @get:MockBean(PrivateHolidayPeriodUpdateUseCase::class)
    internal val privateHolidayPeriodUpdateUseCase = mock<PrivateHolidayPeriodUpdateUseCase>()

    @get:MockBean(PrivateHolidayPeriodDeleteUseCase::class)
    internal val privateHolidayPeriodDeleteUseCase = mock<PrivateHolidayPeriodDeleteUseCase>()

    @BeforeAll
    fun setUp() {
        client = httpClient.toBlocking()
    }

    @Test
    fun `get the vacations by charge year`() {
        doReturn(HOLIDAY_RESPONSE_DTO).whenever(privateHolidaysByChargeYearUseCase).get(CURRENT_YEAR)

        val response = client.exchangeObject<HolidayResponse>(
            GET("/api/vacations?chargeYear=$CURRENT_YEAR")
        )

        assertEquals(OK, response.status())
        assertEquals(HOLIDAY_RESPONSE, response.body.get())
    }

    @Test
    fun `get user vacation details`() {
        doReturn(HOLIDAY_RESPONSE_DTO).whenever(privateHolidaysByChargeYearUseCase).get(CURRENT_YEAR)
        doReturn(VACATION_DETAILS_DTO)
            .whenever(privateHolidayDetailsUseCase).get(CURRENT_YEAR, HOLIDAY_RESPONSE_DTO.vacations)

        val response = client.exchangeObject<VacationDetailsResponse>(
            GET("/api/vacations/details?chargeYear=$CURRENT_YEAR")
        )

        assertEquals(OK, response.status)
        assertEquals(VACATION_DETAILS_RESPONSE, response.body.get())
    }

    @Test
    fun `fail when query param is not suitable to get user vacation details`() {
        val invalidChargeYear = "202xxx"

        val ex = assertThrows<HttpClientResponseException> {
            client.exchangeObject<Any>(
                GET("/api/vacations?chargeYear=$invalidChargeYear")
            )
        }

        assertEquals(BAD_REQUEST, ex.status)
    }

    @Test
    fun `post a new vacation period`() {
        doReturn(listOf(CREATE_VACATION_RESPONSE_DTO))
            .whenever(privateHolidayPeriodCreateUseCase).create(REQUEST_VACATION_DTO, EN_LOCALE)

        val response = client.exchangeList<CreateVacationResponse>(
            POST("/api/vacations", REQUEST_VACATION).header(ACCEPT_LANGUAGE, "en")
        )

        assertEquals(OK, response.status)
        assertEquals(listOf(CREATE_VACATION_RESPONSE), response.body.get())
    }

    private fun postFailProvider() = arrayOf(
        arrayOf(DateRangeException(TODAY, TODAY.minusDays(1)), BAD_REQUEST, "INVALID_DATE_RANGE"),
        arrayOf(VacationRangeClosedException(), BAD_REQUEST, "VACATION_RANGE_CLOSED"),
        arrayOf(VacationBeforeHiringDateException(), BAD_REQUEST, "VACATION_BEFORE_HIRING_DATE"),
        arrayOf(VacationRequestOverlapsException(), BAD_REQUEST, "VACATION_REQUEST_OVERLAPS"),
        arrayOf(VacationRequestEmptyException(), BAD_REQUEST, "VACATION_REQUEST_EMPTY"),
        arrayOf(MaxNextYearRequestVacationException(), BAD_REQUEST, "INVALID_NEXT_YEAR_VACATION_DAYS_REQUEST"),
    )

    @ParameterizedTest
    @MethodSource("postFailProvider")
    fun `fail if try to post a vacation and a exception is throw`(
        exception: Exception,
        expectedResponseStatus: HttpStatus,
        expectedErrorCode: String,
    ) {
        doThrow(exception).whenever(privateHolidayPeriodCreateUseCase).create(REQUEST_VACATION_DTO, EN_LOCALE)

        val ex = assertThrows<HttpClientResponseException> {
            client.exchangeObject<ErrorResponse>(
                POST("/api/vacations", REQUEST_VACATION).header(ACCEPT_LANGUAGE, "en")
            )
        }

        assertEquals(expectedResponseStatus, ex.status)
        assertEquals(expectedErrorCode, ex.response.getBody<ErrorResponse>().get().code)
    }

    @Test
    fun `update a vacation period`() {
        val createVacationResponseDTOs = listOf(CREATE_VACATION_RESPONSE_DTO)
        doReturn(createVacationResponseDTOs)
            .whenever(privateHolidayPeriodUpdateUseCase).update(REQUEST_VACATION_DTO, EN_LOCALE)

        val response = client.exchangeList<CreateVacationResponseDTO>(
            PUT("/api/vacations", REQUEST_VACATION).header(ACCEPT_LANGUAGE, "en")
        )

        assertEquals(OK, response.status)
        assertEquals(createVacationResponseDTOs, response.body.get())
    }

    private fun putFailProvider() = arrayOf(
        arrayOf(UserPermissionException(), NOT_FOUND, "RESOURCE_NOT_FOUND"),
        arrayOf(VacationNotFoundException(1), NOT_FOUND, "RESOURCE_NOT_FOUND"),
        arrayOf(DateRangeException(TODAY, TODAY.minusDays(1)), BAD_REQUEST, "INVALID_DATE_RANGE"),
        arrayOf(VacationAcceptedPastPeriodStateException(), BAD_REQUEST, "VACATION_ALREADY_ACCEPTED_FOR_PAST_PERIOD"),
        arrayOf(VacationRangeClosedException(), BAD_REQUEST, "VACATION_RANGE_CLOSED"),
        arrayOf(VacationBeforeHiringDateException(), BAD_REQUEST, "VACATION_BEFORE_HIRING_DATE"),
        arrayOf(VacationRequestOverlapsException(), BAD_REQUEST, "VACATION_REQUEST_OVERLAPS"),
        arrayOf(VacationRequestEmptyException(), BAD_REQUEST, "VACATION_REQUEST_EMPTY"),
    )

    @ParameterizedTest
    @MethodSource("putFailProvider")
    fun `fail if try to put a vacation and a exception is throw`(
        exception: Exception,
        expectedResponseStatus: HttpStatus,
        expectedErrorCode: String,
    ) {
        doThrow(exception).whenever(privateHolidayPeriodCreateUseCase).create(REQUEST_VACATION_DTO, EN_LOCALE)

        val ex = assertThrows<HttpClientResponseException> {
            client.exchangeObject<Any>(
                POST("/api/vacations", REQUEST_VACATION).header(ACCEPT_LANGUAGE, "en")
            )
        }

        assertEquals(expectedResponseStatus, ex.status)
        assertEquals(expectedErrorCode, ex.response.getBody<ErrorResponse>().get().code)
    }

    @Test
    fun `delete a vacation period`() {
        val response = client.exchangeObject<Unit>(
            DELETE<Unit>("/api/vacations/${VACATION_DTO.id!!}")
        )

        assertEquals(OK, response.status)
        verify(privateHolidayPeriodDeleteUseCase).delete(VACATION_DTO.id!!)
    }

    private fun deleteFailProvider() = arrayOf(
        arrayOf(UserPermissionException(), NOT_FOUND, "RESOURCE_NOT_FOUND"),
        arrayOf(VacationNotFoundException(1), NOT_FOUND, "RESOURCE_NOT_FOUND"),
        arrayOf(VacationRangeClosedException(), BAD_REQUEST, "VACATION_RANGE_CLOSED"),
        arrayOf(VacationAcceptedPastPeriodStateException(), BAD_REQUEST, "VACATION_ALREADY_ACCEPTED_FOR_PAST_PERIOD")
    )

    @ParameterizedTest
    @MethodSource("deleteFailProvider")
    fun `fail if try to delete a vacation and a exception is throw`(
        exception: Exception,
        expectedResponseStatus: HttpStatus,
        expectedErrorCode: String,
    ) {
        doThrow(exception).whenever(privateHolidayPeriodCreateUseCase).create(REQUEST_VACATION_DTO, EN_LOCALE)

        val ex = assertThrows<HttpClientResponseException> {
            client.exchangeObject<Any>(
                POST("/api/vacations", REQUEST_VACATION).header(ACCEPT_LANGUAGE, "en")
            )
        }

        assertEquals(expectedResponseStatus, ex.status)
        assertEquals(expectedErrorCode, ex.response.getBody<ErrorResponse>().get().code)
    }

    private companion object {
        private val TODAY = LocalDate.now()
        private val CURRENT_YEAR = TODAY.year

        private val EN_LOCALE = Locale.ENGLISH

        private val REQUEST_VACATION_DTO = RequestVacationDTO(null, TODAY, TODAY, "Description")


        private val CREATE_VACATION_RESPONSE_DTO =
            CreateVacationResponseDTO(
                REQUEST_VACATION_DTO.startDate,
                REQUEST_VACATION_DTO.endDate,
                DAYS.between(REQUEST_VACATION_DTO.startDate, REQUEST_VACATION_DTO.endDate).toInt(),
                REQUEST_VACATION_DTO.startDate.year
            )

        private val CREATE_VACATION_RESPONSE =
            CreateVacationResponse(
                REQUEST_VACATION_DTO.startDate,
                REQUEST_VACATION_DTO.endDate,
                DAYS.between(REQUEST_VACATION_DTO.startDate, REQUEST_VACATION_DTO.endDate).toInt(),
                REQUEST_VACATION_DTO.startDate.year
            )

        val VACATION_DTO = VacationDTO(
            2,
            "Observations",
            "Description",
            VacationState.PENDING,
            CREATE_VACATION_RESPONSE_DTO.startDate,
            CREATE_VACATION_RESPONSE_DTO.endDate,
            listOf(CREATE_VACATION_RESPONSE_DTO.startDate),
            CREATE_VACATION_RESPONSE_DTO.startDate
        )
        val VACATION_REQUEST = VacationRequest(
            2,
            "Observations",
            "Description",
            VacationState.PENDING,
            CREATE_VACATION_RESPONSE_DTO.startDate,
            CREATE_VACATION_RESPONSE_DTO.endDate,
            listOf(CREATE_VACATION_RESPONSE_DTO.startDate),
            CREATE_VACATION_RESPONSE_DTO.startDate
        )

        private val HOLIDAY_RESPONSE_DTO = HolidayResponseDTO(
            listOf(HolidayDTO(1, "New year", LocalDate.of(LocalDate.now().year, 1, 1))),
            listOf(VACATION_DTO)
        )

        private val HOLIDAY_RESPONSE = HolidayResponse(
            listOf(HolidayRequest(1, "New year", LocalDate.of(LocalDate.now().year, 1, 1))),
            listOf(VACATION_REQUEST)
        )

        private val VACATION_DETAILS_DTO = VacationDetailsDTO(23, 23, 3, 20)

        private val VACATION_DETAILS_RESPONSE = VacationDetailsResponse(23, 23, 3, 20)

        private val REQUEST_VACATION =
            RequestVacation(
                null,
                TODAY,
                TODAY,
                "Description",
            )
    }

}
