package com.autentia.tnt.api.binnacle.activity

import com.autentia.tnt.api.binnacle.*
import com.autentia.tnt.binnacle.entities.dto.SubcontractedActivityFilterDTO
import com.autentia.tnt.binnacle.entities.dto.SubcontractedActivityResponseDTO
import com.autentia.tnt.binnacle.exception.*
import com.autentia.tnt.binnacle.usecases.*
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.YearMonth
import java.util.*

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class SubcontractedActivityControllerIT {

    @Inject
    @field:Client(value = "/", errorType = String::class)
    private lateinit var httpClient: HttpClient

    private lateinit var client: BlockingHttpClient

    @get:MockBean(SubcontractedActivityCreationUseCase::class)
    internal val subcontractedActivityCreationUseCase = mock<SubcontractedActivityCreationUseCase>()

    @get:MockBean(SubcontractedActivityUpdateUseCase::class)
    internal val subcontractedActivityUpdateUseCase = mock<SubcontractedActivityUpdateUseCase>()

    @get:MockBean(SubcontractedActivityDeletionUseCase::class)
    internal val subcontractedActivityDeletionUseCase = mock<SubcontractedActivityDeletionUseCase>()

    @get:MockBean(SubcontractedActivityRetrievalByIdUseCase::class)
    internal val subcontractedActivityRetrievalByIdUseCase = mock<SubcontractedActivityRetrievalByIdUseCase>()

    @get:MockBean(SubcontractedActivitiesByFilterUseCase::class)
    internal val subcontractedActivitiesByFilterUseCase = mock<SubcontractedActivitiesByFilterUseCase>()

    @BeforeAll
    fun setup() {
        client = httpClient.toBlocking()
    }

    @Test
    fun `get all subcontracted activities between the start and end date`() {
        val startDate = LocalDate.of(2018, Month.JANUARY, 1)
        val endDate = LocalDate.of(2018, Month.JANUARY, 31)
        val activityResponseDTOs = listOf(SUBCONTRACTED_ACTIVITY_RESPONSE_DTO)
        val activities = listOf(SUBCONTRACTED_ACTIVITY_RESPONSE)

        whenever(
            subcontractedActivitiesByFilterUseCase.getActivities(
                SubcontractedActivityFilterDTO(
                    startDate = startDate, endDate = endDate
                )
            )
        ).thenReturn(activityResponseDTOs)

        val response = client.exchangeList<SubcontractedActivityResponse>(
            HttpRequest.GET("/api/subcontracted-activity?startDate=${startDate.toJson()}&endDate=${endDate.toJson()}"),
        )

        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.OK, response.status)
        org.junit.jupiter.api.Assertions.assertEquals(activities, response.body.get())
    }




    @Test
    fun `get subcontracted activities by filter`() {
        val startDate = LocalDate.of(2018, Month.JANUARY, 1)
        val endDate = LocalDate.of(2018, Month.JANUARY, 31)
        val organizationId = 1L
        val projectId = 1L
        val roleId = 1L
        val subcontractedActivitiesFilter = SubcontractedActivityFilterDTO(
            startDate,
            endDate,
            organizationId,
            projectId,
            roleId
        )
        val subcontractedActivityResponseDTOs = listOf(SUBCONTRACTED_ACTIVITY_RESPONSE_DTO)
        val subcontractedActivities = listOf(SUBCONTRACTED_ACTIVITY_RESPONSE)

        whenever(subcontractedActivitiesByFilterUseCase.getActivities(subcontractedActivitiesFilter)).thenReturn(subcontractedActivityResponseDTOs)

        val response = client.exchangeList<SubcontractedActivityResponse>(
            HttpRequest.GET(
                "/api/subcontracted-activity?" + "&startDate=${startDate.toJson()}" + "&endDate=${endDate.toJson()}" + "&organizationId=${organizationId}" + "&projectId=${projectId}" + "&roleId=${roleId}"
            ),
        )

        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.OK, response.status)
        org.junit.jupiter.api.Assertions.assertEquals(subcontractedActivities, response.body.get())
    }



    @Test
    fun `get subcontracted activity by id`() {

        whenever(subcontractedActivityRetrievalByIdUseCase.getActivityById(any())).thenReturn(SUBCONTRACTED_ACTIVITY_RESPONSE_DTO)

        val response = client.exchangeObject<SubcontractedActivityResponse>(
            HttpRequest.GET("/api/subcontracted-activity/${SUBCONTRACTED_ACTIVITY_RESPONSE_DTO.id}")
        )

        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.OK, response.status)
        org.junit.jupiter.api.Assertions.assertEquals(SUBCONTRACTED_ACTIVITY_RESPONSE, response.body.get())
    }

    @Test
    fun `fail if try to get a subcontracted activity with a non existing id`() {
        val nonExistingId = 8L

        doThrow(ActivityNotFoundException(1L)).whenever(subcontractedActivityRetrievalByIdUseCase).getActivityById(nonExistingId)

        val ex = assertThrows<HttpClientResponseException> {
            client.exchangeObject<Any>(
                HttpRequest.GET("/api/subcontracted-activity/$nonExistingId"),
            )
        }

        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.NOT_FOUND, ex.status)
        org.junit.jupiter.api.Assertions.assertEquals(
            "RESOURCE_NOT_FOUND",
            ex.response.getBody<ErrorResponse>().get().code
        )
    }

    @Test
    fun `fail if try to post a subcontracted activity with too long description`() {
        val tooLongDescriptionJson = SUBCONTRACTED_ACTIVITY_POST_JSON.replace(
            SUBCONTRACTED_ACTIVITY_REQUEST_BODY_DTO.description, "x".repeat(2049)
        )

        val ex = assertThrows<HttpClientResponseException> {
            client.exchangeObject<Any>(
                HttpRequest.POST("/api/subcontracted-activity", tooLongDescriptionJson).header(HttpHeaders.ACCEPT_LANGUAGE, "en"),
            )
        }

        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.BAD_REQUEST, ex.status)
    }

    private fun postFailProvider() = arrayOf(
        arrayOf(UserPermissionException(), HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND"),
        arrayOf(ProjectRoleNotFoundException(1), HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND"),
        arrayOf(ActivityPeriodClosedException(), HttpStatus.BAD_REQUEST, "ACTIVITY_PERIOD_CLOSED"),
        arrayOf(ProjectClosedException(), HttpStatus.BAD_REQUEST, "CLOSED_PROJECT"),
        arrayOf(ProjectBlockedException(LocalDate.now()), HttpStatus.BAD_REQUEST, "BLOCKED_PROJECT"),
    )

    @ParameterizedTest
    @MethodSource("postFailProvider")
    fun `fail if try to post a subcontracted activity and a exception is throw, result on a HTTP error`(
        exception: Exception,
        expectedResponseStatus: HttpStatus,
        expectedErrorCode: String,
    ) {
        doThrow(exception).whenever(subcontractedActivityCreationUseCase).createSubcontractedActivity(any(), eq(Locale.ENGLISH))

        val ex = assertThrows<HttpClientResponseException> {
            client.exchangeObject<Any>(
                HttpRequest.POST("/api/subcontracted-activity", SUBCONTRACTED_ACTIVITY_POST_JSON).header(HttpHeaders.ACCEPT_LANGUAGE, "en"),
            )
        }

        org.junit.jupiter.api.Assertions.assertEquals(expectedResponseStatus, ex.status)
        org.junit.jupiter.api.Assertions.assertEquals(
            expectedErrorCode,
            ex.response.getBody<ErrorResponse>().get().code
        )
    }

    @Test
    fun `put an subcontracted activity`() {
        val putActivity = SUBCONTRACTED_ACTIVITY_REQUEST_BODY_DTO.copy(
            id = SUBCONTRACTED_ACTIVITY_RESPONSE_DTO.id, description = "Updated activity description"
        )
        val updatedActivity = SUBCONTRACTED_ACTIVITY_RESPONSE_DTO.copy(
            description = putActivity.description
        )
        val updatedActivityResponse = SubcontractedActivityResponse.from(updatedActivity)
        doReturn(updatedActivity).whenever(subcontractedActivityUpdateUseCase).updateSubcontractedActivity(any(), eq(Locale.ENGLISH))

        val response = client.exchangeObject<SubcontractedActivityResponse>(
            HttpRequest.PUT("/api/subcontracted-activity", SUBCONTRACTED_ACTIVITY_PUT_JSON).header(HttpHeaders.ACCEPT_LANGUAGE, "en"),
        )

        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.OK, response.status)
        org.junit.jupiter.api.Assertions.assertEquals(updatedActivityResponse, response.body.get())
    }

    private fun putFailProvider() = arrayOf(
        arrayOf(UserPermissionException(), HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND"),
        arrayOf(ActivityNotFoundException(1), HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND"),
        arrayOf(ProjectRoleNotFoundException(1), HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND"),
        arrayOf(ActivityPeriodClosedException(), HttpStatus.BAD_REQUEST, "ACTIVITY_PERIOD_CLOSED"),
        arrayOf(ProjectClosedException(), HttpStatus.BAD_REQUEST, "CLOSED_PROJECT"),
        arrayOf(ProjectBlockedException(LocalDate.now()), HttpStatus.BAD_REQUEST, "BLOCKED_PROJECT"),
    )

    @ParameterizedTest
    @MethodSource("putFailProvider")
    fun `fail if try to put a subcontracted activity and exception is thrown, result on a HTTP error`(
        exception: Exception,
        expectedResponseStatus: HttpStatus,
        expectedErrorCode: String,
    ) {
        doThrow(exception).whenever(subcontractedActivityUpdateUseCase).updateSubcontractedActivity(any(), eq(Locale.ENGLISH))

        val ex = assertThrows<HttpClientResponseException> {
            client.exchangeObject<Any>(
                HttpRequest.PUT("/api/subcontracted-activity", SUBCONTRACTED_ACTIVITY_POST_JSON).header(HttpHeaders.ACCEPT_LANGUAGE, "en"),
            )
        }

        org.junit.jupiter.api.Assertions.assertEquals(expectedResponseStatus, ex.status)
        org.junit.jupiter.api.Assertions.assertEquals(
            expectedErrorCode,
            ex.response.getBody<ErrorResponse>().get().code
        )
    }

    @Test
    fun `delete a subcontracted activity`() {
        val activityIdToDelete = 14L

        val response = client.exchange<Any, Any>(
            HttpRequest.DELETE("/api/subcontracted-activity/$activityIdToDelete")
        )

        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.OK, response.status)
        verify(subcontractedActivityDeletionUseCase).deleteSubcontractedActivityById(activityIdToDelete)
    }

    private fun deleteFailProvider() = arrayOf(
        arrayOf(UserPermissionException(), HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND"),
        arrayOf(ActivityNotFoundException(1), HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND"),
        arrayOf(ActivityPeriodClosedException(), HttpStatus.BAD_REQUEST, "ACTIVITY_PERIOD_CLOSED"),
        arrayOf(ProjectBlockedException(LocalDate.now()), HttpStatus.BAD_REQUEST, "BLOCKED_PROJECT"),
    )

    @ParameterizedTest
    @MethodSource("deleteFailProvider")
    fun `fail if try to delete a subcontracted activity and exception is throw, result on a HTTP error`(
        exception: Exception,
        expectedResponseStatus: HttpStatus,
        expectedErrorCode: String,
    ) {
        doThrow(exception).whenever(subcontractedActivityDeletionUseCase).deleteSubcontractedActivityById(SUBCONTRACTED_ACTIVITY_RESPONSE_DTO.id)

        val ex = assertThrows<HttpClientResponseException> {
            client.exchangeObject<Unit>(
                HttpRequest.DELETE("/api/subcontracted-activity/${SUBCONTRACTED_ACTIVITY_RESPONSE_DTO.id}"),
            )
        }

        org.junit.jupiter.api.Assertions.assertEquals(expectedResponseStatus, ex.status)
        org.junit.jupiter.api.Assertions.assertEquals(
            expectedErrorCode,
            ex.response.getBody<ErrorResponse>().get().code
        )
    }







    private companion object {

        private val START_DATE = LocalDateTime.of(2014, Month.JANUARY, 1, 8, 0)
        private val DURATION = 18000

        private val INTERVAL_REQUEST_DTO = YearMonth.of(START_DATE.year, START_DATE.month)

        private val SUBCONTRACTED_ACTIVITY_REQUEST_BODY_DTO = SubcontractedActivityRequest(
            null, INTERVAL_REQUEST_DTO, DURATION, "Activity description", 3
        )



        private val SUBCONTRACTED_ACTIVITY_POST_JSON = """
            {
                "month": "${SUBCONTRACTED_ACTIVITY_REQUEST_BODY_DTO.month}",
                "duration": ${SUBCONTRACTED_ACTIVITY_REQUEST_BODY_DTO.duration},        
                "description": "${SUBCONTRACTED_ACTIVITY_REQUEST_BODY_DTO.description}",
                "projectRoleId": ${SUBCONTRACTED_ACTIVITY_REQUEST_BODY_DTO.projectRoleId}             
            }
        """.trimIndent()

        private val SUBCONTRACTED_ACTIVITY_RESPONSE_DTO = SubcontractedActivityResponseDTO(
            SUBCONTRACTED_ACTIVITY_REQUEST_BODY_DTO.duration,
            SUBCONTRACTED_ACTIVITY_REQUEST_BODY_DTO.description,
            2L,
            SUBCONTRACTED_ACTIVITY_REQUEST_BODY_DTO.projectRoleId,
            SUBCONTRACTED_ACTIVITY_REQUEST_BODY_DTO.month,
            42
        )
        private val SUBCONTRACTED_ACTIVITY_RESPONSE = SubcontractedActivityResponse.from(SUBCONTRACTED_ACTIVITY_RESPONSE_DTO)

        private val SUBCONTRACTED_ACTIVITY_PUT_JSON = """
            {
                "id": ${SUBCONTRACTED_ACTIVITY_RESPONSE_DTO.id},
                "month": "${SUBCONTRACTED_ACTIVITY_RESPONSE_DTO.month}",                                   
                "duration": ${SUBCONTRACTED_ACTIVITY_RESPONSE_DTO.duration},
                "description": "Updated activity description",
                "projectRoleId": ${SUBCONTRACTED_ACTIVITY_RESPONSE_DTO.projectRoleId}
            }
        """.trimIndent()

    }

}