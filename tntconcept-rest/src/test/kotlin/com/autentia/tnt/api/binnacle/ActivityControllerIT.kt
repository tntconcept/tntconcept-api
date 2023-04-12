package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.entities.dto.*
import com.autentia.tnt.binnacle.exception.*
import com.autentia.tnt.binnacle.usecases.*
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
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month.JANUARY
import java.util.*

@MicronautTest
@TestInstance(PER_CLASS)
internal class ActivityControllerIT {

    @Inject
    @field:Client("/")
    private lateinit var httpClient: HttpClient

    private lateinit var client: BlockingHttpClient

    @get:MockBean(ActivitiesBetweenDateUseCase::class)
    internal val activitiesBetweenDateUseCase = mock<ActivitiesBetweenDateUseCase>()

    @get:MockBean(ActivityRetrievalByIdUseCase::class)
    internal val activityRetrievalUseCase = mock<ActivityRetrievalByIdUseCase>()

    @get:MockBean(ActivityCreationUseCase::class)
    internal val activityCreationUseCase = mock<ActivityCreationUseCase>()

    @get:MockBean(ActivityUpdateUseCase::class)
    internal val activityUpdateUseCase = mock<ActivityUpdateUseCase>()

    @get:MockBean(ActivityDeletionUseCase::class)
    internal val activityDeletionUseCase = mock<ActivityDeletionUseCase>()

    @get:MockBean(ActivityImageRetrievalUseCase::class)
    internal val activityImageRetrievalUseCase = mock<ActivityImageRetrievalUseCase>()

    @get:MockBean(ActivitiesSummaryUseCase::class)
    internal val activitiesSummaryUseCase = mock<ActivitiesSummaryUseCase>()

    @get:MockBean(ActivityApprovalUseCase::class)
    internal val activityApprovalUseCase = mock<ActivityApprovalUseCase>()

    @get:MockBean(ActivitiesByApprovalStateUseCase::class)
    internal val activitiesByApprovalStateUseCase = mock<ActivitiesByApprovalStateUseCase>()

    @BeforeAll
    fun setup() {
        client = httpClient.toBlocking()
    }

    @Test
    fun `get all activities between the start and end date`() {
        val startDate = LocalDate.of(2018, JANUARY, 1)
        val endDate = LocalDate.of(2018, JANUARY, 31)
        val activities = listOf(ACTIVITY_RESPONSE_DTO)

        doReturn(activities).whenever(activitiesBetweenDateUseCase).getActivities(startDate, endDate)

        val response = client.exchangeList<ActivityResponseDTO>(
            GET("/api/activity?startDate=${startDate.toJson()}&endDate=${endDate.toJson()}"),
        )

        assertEquals(OK, response.status)
        assertEquals(activities, response.body.get())
    }

    @Test
    fun `get all activities by approvalState`() {
        val approvalState = ApprovalState.PENDING
        val activities = listOf(ACTIVITY_RESPONSE_DTO)

        whenever(activitiesByApprovalStateUseCase.getActivities(approvalState)).thenReturn(activities)

        val response = client.exchangeList<ActivityResponseDTO>(
            GET("/api/activity?approvalState=${approvalState}"),
        )

        assertEquals(OK, response.status)
        assertEquals(activities, response.body.get())
    }

    @Test
    fun `get all activities should return bad request response`() {
        val expectedErrorCode = ErrorResponse("ILLEGAL_ARGUMENT", "Invalid parameters")
        val ex = assertThrows<HttpClientResponseException> {
            client.exchangeList<ActivityResponseDTO>(
                GET("/api/activity"),
            )
        }

        assertEquals(BAD_REQUEST, ex.status)
        assertEquals(expectedErrorCode, ex.response.getBody<ErrorResponse>().get())
    }

    @Test
    fun `get summary activities between the start and end date`() {
        val startDate = LocalDate.of(2018, JANUARY, 1)
        val endDate = LocalDate.of(2018, JANUARY, 31)
        val activities = listOf(ACTIVITY_RESPONSE_DTO)
        doReturn(activities).whenever(activitiesSummaryUseCase).getActivitiesSummary(startDate, endDate)

        val response = client.exchangeList<ActivityResponseDTO>(
            GET("/api/activity/summary?startDate=${startDate.toJson()}&endDate=${endDate.toJson()}"),
        )

        assertEquals(OK, response.status)
        assertEquals(activities, response.body.get())
    }

    @Test
    fun `get activity by id`() {
        doReturn(ACTIVITY_RESPONSE_DTO).whenever(activityRetrievalUseCase).getActivityById(ACTIVITY_RESPONSE_DTO.id)

        val response = client.exchangeObject<ActivityResponseDTO>(
            GET("/api/activity/${ACTIVITY_RESPONSE_DTO.id}")
        )

        assertEquals(OK, response.status)
        assertEquals(ACTIVITY_RESPONSE_DTO, response.body.get())
    }

    @Test
    fun `fail if try to get an activity with a non existing id`() {
        val nonExistingId = 8L
        doReturn(null).whenever(activityRetrievalUseCase).getActivityById(nonExistingId)

        val ex = assertThrows<HttpClientResponseException> {
            client.exchangeObject<Any>(
                GET("/api/activity/$nonExistingId"),
            )
        }

        assertEquals(NOT_FOUND, ex.status)
    }

    @Test
    fun `get an image's activity by id`() {
        val userId = ACTIVITY_RESPONSE_DTO.userId
        doReturn(ACTIVITY_IMAGE).whenever(activityImageRetrievalUseCase).getActivityImage(userId)

        val response = client.exchangeObject<String>(
            GET("/api/activity/$userId/image")
        )

        assertEquals(OK, response.status)
        assertEquals(ACTIVITY_IMAGE, response.body())
    }

    @Test
    fun `post a new activity`() {
        doReturn(ACTIVITY_RESPONSE_DTO).whenever(activityCreationUseCase).createActivity(ACTIVITY_REQUEST_BODY_DTO)

        val response = client.exchangeObject<ActivityResponseDTO>(
            POST("/api/activity", ACTIVITY_POST_JSON)
        )

        assertEquals(OK, response.status)
        assertEquals(ACTIVITY_RESPONSE_DTO, response.body.get())
    }

    @Test
    fun `fail if try to post activity with too long description`() {
        val tooLongDescriptionJson = ACTIVITY_POST_JSON.replace(
            ACTIVITY_REQUEST_BODY_DTO.description,
            "x".repeat(2049)
        )

        val ex = assertThrows<HttpClientResponseException> {
            client.exchangeObject<Any>(
                POST("/api/activity", tooLongDescriptionJson),
            )
        }

        assertEquals(BAD_REQUEST, ex.status)
    }

    private fun postFailProvider() = arrayOf(
        arrayOf(UserPermissionException(), NOT_FOUND, "RESOURCE_NOT_FOUND"),
        arrayOf(ProjectRoleNotFoundException(1), NOT_FOUND, "RESOURCE_NOT_FOUND"),
        arrayOf(ActivityPeriodClosedException(), BAD_REQUEST, "ACTIVITY_PERIOD_CLOSED"),
        arrayOf(OverlapsAnotherTimeException(), BAD_REQUEST, "ACTIVITY_TIME_OVERLAPS"),
        arrayOf(ProjectClosedException(), BAD_REQUEST, "CLOSED_PROJECT"),
        arrayOf(ActivityBeforeHiringDateException(), BAD_REQUEST, "ACTIVITY_BEFORE_HIRING_DATE")
    )

    @ParameterizedTest
    @MethodSource("postFailProvider")
    fun `fail if try to post an activity and a exception is throw`(
        exception: Exception,
        expectedResponseStatus: HttpStatus,
        expectedErrorCode: String
    ) {
        doThrow(exception).whenever(activityCreationUseCase).createActivity(ACTIVITY_REQUEST_BODY_DTO)

        val ex = assertThrows<HttpClientResponseException> {
            client.exchangeObject<Any>(
                POST("/api/activity", ACTIVITY_POST_JSON),
            )
        }

        assertEquals(expectedResponseStatus, ex.status)
        assertEquals(expectedErrorCode, ex.response.getBody<ErrorResponse>().get().code)
    }

    @Test
    fun `put an activity`() {
        val putActivity = ACTIVITY_REQUEST_BODY_DTO.copy(
            id = ACTIVITY_RESPONSE_DTO.id,
            description = "Updated activity description"
        )
        val updatedActivity = ACTIVITY_RESPONSE_DTO.copy(
            description = putActivity.description
        )
        doReturn(updatedActivity).whenever(activityUpdateUseCase).updateActivity(putActivity)

        val response = client.exchangeObject<ActivityResponseDTO>(
            PUT("/api/activity", ACTIVITY_PUT_JSON),
        )

        assertEquals(OK, response.status)
        assertEquals(updatedActivity, response.body.get())
    }

    private fun putFailProvider() = arrayOf(
        arrayOf(UserPermissionException(), NOT_FOUND, "RESOURCE_NOT_FOUND"),
        arrayOf(ActivityNotFoundException(1), NOT_FOUND, "RESOURCE_NOT_FOUND"),
        arrayOf(ProjectRoleNotFoundException(1), NOT_FOUND, "RESOURCE_NOT_FOUND"),
        arrayOf(ActivityPeriodClosedException(), BAD_REQUEST, "ACTIVITY_PERIOD_CLOSED"),
        arrayOf(OverlapsAnotherTimeException(), BAD_REQUEST, "ACTIVITY_TIME_OVERLAPS"),
        arrayOf(ProjectClosedException(), BAD_REQUEST, "CLOSED_PROJECT"),
        arrayOf(ActivityBeforeHiringDateException(), BAD_REQUEST, "ACTIVITY_BEFORE_HIRING_DATE")
    )

    @ParameterizedTest
    @MethodSource("putFailProvider")
    fun `fail if try to put an activity and exception is throw`(
        exception: Exception,
        expectedResponseStatus: HttpStatus,
        expectedErrorCode: String
    ) {
        doThrow(exception).whenever(activityUpdateUseCase).updateActivity(ACTIVITY_REQUEST_BODY_DTO)

        val ex = assertThrows<HttpClientResponseException> {
            client.exchangeObject<Any>(
                PUT("/api/activity", ACTIVITY_POST_JSON),
            )
        }

        assertEquals(expectedResponseStatus, ex.status)
        assertEquals(expectedErrorCode, ex.response.getBody<ErrorResponse>().get().code)
    }

    @Test
    fun `delete an activity`() {
        val activityIdToDelete = 14L

        val response = client.exchange<Any, Any>(
            DELETE("/api/activity/$activityIdToDelete")
        )

        assertEquals(OK, response.status)
        verify(activityDeletionUseCase).deleteActivityById(activityIdToDelete)
    }

    private fun deleteFailProvider() = arrayOf(
        arrayOf(UserPermissionException(), NOT_FOUND, "RESOURCE_NOT_FOUND"),
        arrayOf(ActivityNotFoundException(1), NOT_FOUND, "RESOURCE_NOT_FOUND"),
        arrayOf(ActivityPeriodClosedException(), BAD_REQUEST, "ACTIVITY_PERIOD_CLOSED")
    )

    @ParameterizedTest
    @MethodSource("deleteFailProvider")
    fun `fail if try to delete an activity and exception is throw`(
        exception: Exception,
        expectedResponseStatus: HttpStatus,
        expectedErrorCode: String
    ) {
        doThrow(exception).whenever(activityDeletionUseCase).deleteActivityById(ACTIVITY_RESPONSE_DTO.id)

        val ex = assertThrows<HttpClientResponseException> {
            client.exchangeObject<Unit>(
                DELETE("/api/activity/${ACTIVITY_RESPONSE_DTO.id}"),
            )
        }

        assertEquals(expectedResponseStatus, ex.status)
        assertEquals(expectedErrorCode, ex.response.getBody<ErrorResponse>().get().code)
    }

    @Test
    fun `approve an activity`() {
        doReturn(ACTIVITY_RESPONSE_DTO).whenever(activityApprovalUseCase).approveActivity(ACTIVITY_RESPONSE_DTO.id)

        val response = client.exchangeObject<ActivityResponseDTO>(
            POST("/api/activity/${ACTIVITY_RESPONSE_DTO.id}/approve", "")
        )

        assertEquals(OK, response.status)
        assertEquals(ACTIVITY_RESPONSE_DTO, response.body.get())
    }

    private fun activityApprovalFailedProvider() = arrayOf(
        arrayOf(UserPermissionException(), NOT_FOUND, ErrorResponse("RESOURCE_NOT_FOUND", "You don't have permission to access the resource")),
        arrayOf(InvalidActivityApprovalStateException(), CONFLICT, ErrorResponse("INVALID_ACTIVITY_APPROVAL_STATE", "Activity could not been approved"))
    )

    @ParameterizedTest
    @MethodSource("activityApprovalFailedProvider")
    fun `fail if try to approve an activity and exception is throw`(
        exception: Exception,
        expectedResponseStatus: HttpStatus,
        expectedErrorResponse: ErrorResponse?,
    ) {
        doThrow(exception).whenever(activityApprovalUseCase).approveActivity(ACTIVITY_RESPONSE_DTO.id)

        val ex = assertThrows<HttpClientResponseException> {
            client.exchangeObject<Unit>(
                POST("/api/activity/${ACTIVITY_RESPONSE_DTO.id}/approve", ""),
            )
        }

        assertEquals(expectedResponseStatus, ex.status)
        assertEquals(expectedErrorResponse, ex.response.getBody<ErrorResponse>().get())
    }


    private companion object {
        private val START_DATE = LocalDateTime.of(2018, JANUARY, 10, 8, 0)
        private val END_DATE = LocalDateTime.of(2018, JANUARY, 10, 12, 0)

        private val INTERVAL_RESPONSE_DTO = IntervalResponseDTO(
            START_DATE,
            END_DATE,
            240,
            TimeUnit.MINUTES
        )

        private val INTERVAL_REQUEST_DTO = TimeIntervalRequestDTO(
            START_DATE,
            END_DATE
        )

        private val ACTIVITY_REQUEST_BODY_DTO = ActivityRequestBodyDTO(
            null,
            INTERVAL_REQUEST_DTO,
            "Activity description",
            true,
            3,
            false,
            null
        )

        private val ACTIVITY_POST_JSON = """
            {
                "interval": {
                    "start": "${ACTIVITY_REQUEST_BODY_DTO.interval.start.toJson()}",
                    "end": "${ACTIVITY_REQUEST_BODY_DTO.interval.end.toJson()}"
                },                
                "description": "${ACTIVITY_REQUEST_BODY_DTO.description}",
                "billable": ${ACTIVITY_REQUEST_BODY_DTO.billable},
                "projectRoleId": ${ACTIVITY_REQUEST_BODY_DTO.projectRoleId},
                "hasEvidences": ${ACTIVITY_REQUEST_BODY_DTO.hasEvidences}                
            }
        """.trimIndent()



        private val ACTIVITY_RESPONSE_DTO = ActivityResponseDTO(
            ACTIVITY_REQUEST_BODY_DTO.billable,
            ACTIVITY_REQUEST_BODY_DTO.description,
            ACTIVITY_REQUEST_BODY_DTO.hasEvidences,
            2L,
            ACTIVITY_REQUEST_BODY_DTO.projectRoleId,
            IntervalResponseDTO(ACTIVITY_REQUEST_BODY_DTO.interval.start,
                ACTIVITY_REQUEST_BODY_DTO.interval.end,
                240, TimeUnit.MINUTES),
            42,
            ApprovalState.ACCEPTED)

        private val ACTIVITY_PUT_JSON = """
            {
                "id": ${ACTIVITY_RESPONSE_DTO.id},
                "interval": {
                    "start": "${ACTIVITY_RESPONSE_DTO.interval.start.toJson()}",
                    "end": "${ACTIVITY_RESPONSE_DTO.interval.end.toJson()}"
                },                                    
                "description": "Updated activity description",
                "billable": ${ACTIVITY_RESPONSE_DTO.billable},
                "projectRoleId": ${ACTIVITY_RESPONSE_DTO.projectRoleId},
                "hasEvidences": ${ACTIVITY_RESPONSE_DTO.hasEvidences}
            }
        """.trimIndent()

        private val ACTIVITY_IMAGE = "base64image"
    }

}
