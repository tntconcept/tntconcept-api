package com.autentia.tnt.api.binnacle.activity

import com.autentia.tnt.api.binnacle.*
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.entities.dto.*
import com.autentia.tnt.binnacle.exception.*
import com.autentia.tnt.binnacle.usecases.*
import io.micronaut.http.HttpHeaders
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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month.JANUARY
import java.util.*

@MicronautTest
@TestInstance(PER_CLASS)
internal class ActivityControllerIT {

    @Inject
    @field:Client(value = "/", errorType = String::class)
    private lateinit var httpClient: HttpClient

    private lateinit var client: BlockingHttpClient

    @get:MockBean(ActivityRetrievalByIdUseCase::class)
    internal val activityRetrievalUseCase = mock<ActivityRetrievalByIdUseCase>()

    @get:MockBean(ActivityCreationUseCase::class)
    internal val activityCreationUseCase = mock<ActivityCreationUseCase>()

    @get:MockBean(ActivityUpdateUseCase::class)
    internal val activityUpdateUseCase = mock<ActivityUpdateUseCase>()

    @get:MockBean(ActivityDeletionUseCase::class)
    internal val activityDeletionUseCase = mock<ActivityDeletionUseCase>()

    @get:MockBean(ActivityEvidenceRetrievalUseCase::class)
    internal val activityEvidenceRetrievalUseCase = mock<ActivityEvidenceRetrievalUseCase>()

    @get:MockBean(ActivitiesSummaryUseCase::class)
    internal val activitiesSummaryUseCase = mock<ActivitiesSummaryUseCase>()

    @get:MockBean(ActivityApprovalUseCase::class)
    internal val activityApprovalUseCase = mock<ActivityApprovalUseCase>()

    @get:MockBean(ActivitiesByFilterUseCase::class)
    internal val activitiesByFilterUseCase = mock<ActivitiesByFilterUseCase>()

    @BeforeAll
    fun setup() {
        client = httpClient.toBlocking()
    }

    @Test
    fun `get all activities between the start and end date`() {
        val startDate = LocalDate.of(2018, JANUARY, 1)
        val endDate = LocalDate.of(2018, JANUARY, 31)
        val activityResponseDTOs = listOf(ACTIVITY_RESPONSE_DTO)
        val activities = listOf(ACTIVITY_RESPONSE)

        whenever(
            activitiesByFilterUseCase.getActivities(
                ActivityFilterDTO(
                    startDate = startDate, endDate = endDate
                )
            )
        ).thenReturn(activityResponseDTOs)

        val response = client.exchangeList<ActivityResponse>(
            GET("/api/activity?startDate=${startDate.toJson()}&endDate=${endDate.toJson()}"),
        )

        assertEquals(OK, response.status)
        assertEquals(activities, response.body.get())
    }

    @Test
    fun `get all pending activities by approvalState`() {
        val approvalState = ApprovalStateActivityFilter.PENDING
        val activityResponseDTOs = listOf(ACTIVITY_RESPONSE_DTO)
        val activities = listOf(ACTIVITY_RESPONSE)

        whenever(activitiesByFilterUseCase.getActivities(ActivityFilterDTO(approvalState = approvalState))).thenReturn(
            activityResponseDTOs
        )

        val response = client.exchangeList<ActivityResponse>(
            GET("/api/activity?approvalState=${approvalState}"),
        )

        assertEquals(OK, response.status)
        assertEquals(activities, response.body.get())
    }


    @Test
    fun `get activities by filter`() {
        val startDate = LocalDate.of(2018, JANUARY, 1)
        val approvalState = ApprovalStateActivityFilter.PENDING
        val endDate = LocalDate.of(2018, JANUARY, 31)
        val organizationId = 1L
        val projectId = 1L
        val roleId = 1L
        val userId = 5L
        val activitiesFilter = ActivityFilterDTO(
            startDate,
            endDate,
            ApprovalStateActivityFilter.PENDING,
            organizationId,
            projectId,
            roleId,
            userId,
        )
        val activityResponseDTOs = listOf(ACTIVITY_RESPONSE_DTO)
        val activities = listOf(ACTIVITY_RESPONSE)

        whenever(activitiesByFilterUseCase.getActivities(activitiesFilter)).thenReturn(activityResponseDTOs)

        val response = client.exchangeList<ActivityResponse>(
            GET(
                "/api/activity?" + "approvalState=${approvalState}" + "&startDate=${startDate.toJson()}" + "&endDate=${endDate.toJson()}" + "&organizationId=${organizationId}" + "&projectId=${projectId}" + "&roleId=${roleId}" + "&userId=${userId}"
            ),
        )

        assertEquals(OK, response.status)
        assertEquals(activities, response.body.get())
    }

    @Test
    fun `get summary activities between the start and end date`() {
        val startDate = LocalDate.of(2018, JANUARY, 1)
        val endDate = LocalDate.of(2018, JANUARY, 31)
        val activitySummaryDTOs = listOf(ACTIVITY_SUMMARY_DTO)
        val activitySummaryResponses = listOf(ACTIVITY_SUMMARY_RESPONSE)
        doReturn(activitySummaryDTOs).whenever(activitiesSummaryUseCase).getActivitiesSummary(startDate, endDate)

        val response = client.exchangeList<ActivitySummaryResponse>(
            GET("/api/activity/summary?startDate=${startDate.toJson()}&endDate=${endDate.toJson()}"),
        )

        assertEquals(OK, response.status)
        assertEquals(activitySummaryResponses, response.body.get())
    }

    @Test
    fun `get activity by id`() {
        doReturn(ACTIVITY_RESPONSE_DTO).whenever(activityRetrievalUseCase).getActivityById(ACTIVITY_RESPONSE_DTO.id)

        val response = client.exchangeObject<ActivityResponse>(
            GET("/api/activity/${ACTIVITY_RESPONSE_DTO.id}")
        )

        assertEquals(OK, response.status)
        assertEquals(ACTIVITY_RESPONSE, response.body.get())
    }

    @Test
    fun `fail if try to get an activity with a non existing id`() {
        val nonExistingId = 8L

        doThrow(ActivityNotFoundException(1L)).whenever(activityRetrievalUseCase).getActivityById(nonExistingId)

        val ex = assertThrows<HttpClientResponseException> {
            client.exchangeObject<Any>(
                GET("/api/activity/$nonExistingId"),
            )
        }

        assertEquals(NOT_FOUND, ex.status)
        assertEquals("RESOURCE_NOT_FOUND", ex.response.getBody<ErrorResponse>().get().code)
    }

    @Test
    fun `get an evidence activity by id`() {
        val activityId = ACTIVITY_RESPONSE_DTO.id
        doReturn(EvidenceDTO.from(ACTIVITY_IMAGE)).whenever(activityEvidenceRetrievalUseCase)
            .getActivityEvidenceByActivityId(activityId)

        val response = client.exchangeObject<String>(
            GET("/api/activity/$activityId/evidence")
        )

        assertEquals(OK, response.status)
        assertEquals(ACTIVITY_IMAGE, response.body())
    }

    @Test
    fun `post a new activity without evidence`() {
        doReturn(ACTIVITY_RESPONSE_DTO).whenever(activityCreationUseCase).createActivity(any(), eq(Locale.ENGLISH))

        val response = client.exchangeObject<ActivityResponse>(
            POST("/api/activity", ACTIVITY_POST_JSON).header(HttpHeaders.ACCEPT_LANGUAGE, "en")
        )

        assertEquals(OK, response.status)
        assertEquals(ACTIVITY_RESPONSE, response.body.get())
    }

    @Test
    fun `post a new activity with evidence`() {
        doReturn(ACTIVITY_RESPONSE_DTO).whenever(activityCreationUseCase).createActivity(any(), eq(Locale.ENGLISH))

        val response = client.exchangeObject<ActivityResponse>(
            POST("/api/activity", ACTIVITY_WITH_EVIDENCE_POST_JSON).header(HttpHeaders.ACCEPT_LANGUAGE, "en")
        )

        assertEquals(OK, response.status)
        assertEquals(ACTIVITY_RESPONSE, response.body.get())
    }

    @Test
    fun `post a new activity with wrong evidence format will result in bad request`() {
        try {
            client.exchangeObject<Any>(
                POST("/api/activity", ACTIVITY_WITH_WRONG_EVIDENCE_POST_JSON).header(HttpHeaders.ACCEPT_LANGUAGE, "en")
            )
        } catch (ex: HttpClientResponseException) {
            assertThat(ex.response.status).isEqualTo(BAD_REQUEST)
        }
    }

    @Test
    fun `fail if try to post activity with too long description`() {
        val tooLongDescriptionJson = ACTIVITY_POST_JSON.replace(
            ACTIVITY_REQUEST_BODY_DTO.description, "x".repeat(2049)
        )

        val ex = assertThrows<HttpClientResponseException> {
            client.exchangeObject<Any>(
                POST("/api/activity", tooLongDescriptionJson).header(HttpHeaders.ACCEPT_LANGUAGE, "en"),
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
        arrayOf(ActivityBeforeHiringDateException(), BAD_REQUEST, "ACTIVITY_BEFORE_HIRING_DATE"),
        arrayOf(ProjectBlockedException(LocalDate.now()), BAD_REQUEST, "BLOCKED_PROJECT"),
    )

    @ParameterizedTest
    @MethodSource("postFailProvider")
    fun `fail if try to post an activity and a exception is throw`(
        exception: Exception,
        expectedResponseStatus: HttpStatus,
        expectedErrorCode: String,
    ) {
        doThrow(exception).whenever(activityCreationUseCase).createActivity(any(), eq(Locale.ENGLISH))

        val ex = assertThrows<HttpClientResponseException> {
            client.exchangeObject<Any>(
                POST("/api/activity", ACTIVITY_POST_JSON).header(HttpHeaders.ACCEPT_LANGUAGE, "en"),
            )
        }

        assertEquals(expectedResponseStatus, ex.status)
        assertEquals(expectedErrorCode, ex.response.getBody<ErrorResponse>().get().code)
    }

    @Test
    fun `put an activity`() {
        val putActivity = ACTIVITY_REQUEST_BODY_DTO.copy(
            id = ACTIVITY_RESPONSE_DTO.id, description = "Updated activity description"
        )
        val updatedActivity = ACTIVITY_RESPONSE_DTO.copy(
            description = putActivity.description
        )
        val updatedActivityResponse = ActivityResponse.from(updatedActivity)
        doReturn(updatedActivity).whenever(activityUpdateUseCase).updateActivity(any(), eq(Locale.ENGLISH))

        val response = client.exchangeObject<ActivityResponse>(
            PUT("/api/activity", ACTIVITY_PUT_JSON).header(HttpHeaders.ACCEPT_LANGUAGE, "en"),
        )

        assertEquals(OK, response.status)
        assertEquals(updatedActivityResponse, response.body.get())
    }

    private fun putFailProvider() = arrayOf(
        arrayOf(UserPermissionException(), NOT_FOUND, "RESOURCE_NOT_FOUND"),
        arrayOf(ActivityNotFoundException(1), NOT_FOUND, "RESOURCE_NOT_FOUND"),
        arrayOf(ProjectRoleNotFoundException(1), NOT_FOUND, "RESOURCE_NOT_FOUND"),
        arrayOf(ActivityPeriodClosedException(), BAD_REQUEST, "ACTIVITY_PERIOD_CLOSED"),
        arrayOf(OverlapsAnotherTimeException(), BAD_REQUEST, "ACTIVITY_TIME_OVERLAPS"),
        arrayOf(ProjectClosedException(), BAD_REQUEST, "CLOSED_PROJECT"),
        arrayOf(ActivityBeforeHiringDateException(), BAD_REQUEST, "ACTIVITY_BEFORE_HIRING_DATE"),
        arrayOf(ProjectBlockedException(LocalDate.now()), BAD_REQUEST, "BLOCKED_PROJECT"),
    )

    @ParameterizedTest
    @MethodSource("putFailProvider")
    fun `fail if try to put an activity and exception is thrown`(
        exception: Exception,
        expectedResponseStatus: HttpStatus,
        expectedErrorCode: String,
    ) {
        doThrow(exception).whenever(activityUpdateUseCase).updateActivity(any(), eq(Locale.ENGLISH))

        val ex = assertThrows<HttpClientResponseException> {
            client.exchangeObject<Any>(
                PUT("/api/activity", ACTIVITY_POST_JSON).header(HttpHeaders.ACCEPT_LANGUAGE, "en"),
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
        arrayOf(ActivityPeriodClosedException(), BAD_REQUEST, "ACTIVITY_PERIOD_CLOSED"),
        arrayOf(ProjectBlockedException(LocalDate.now()), BAD_REQUEST, "BLOCKED_PROJECT"),
    )

    @ParameterizedTest
    @MethodSource("deleteFailProvider")
    fun `fail if try to delete an activity and exception is throw`(
        exception: Exception,
        expectedResponseStatus: HttpStatus,
        expectedErrorCode: String,
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
        doReturn(ACTIVITY_RESPONSE_DTO).whenever(activityApprovalUseCase)
            .approveActivity(ACTIVITY_RESPONSE_DTO.id, Locale.ENGLISH)

        val response = client.exchangeObject<ActivityResponse>(
            POST("/api/activity/${ACTIVITY_RESPONSE_DTO.id}/approve", "").header(HttpHeaders.ACCEPT_LANGUAGE, "en")
        )

        assertEquals(OK, response.status)
        assertEquals(ACTIVITY_RESPONSE, response.body.get())
    }

    private fun activityApprovalFailedProvider() = arrayOf(
        arrayOf(
            UserPermissionException(),
            NOT_FOUND,
            ErrorResponse("RESOURCE_NOT_FOUND", "You don't have permission to access the resource")
        ), arrayOf(
            InvalidActivityApprovalStateException(),
            CONFLICT,
            ErrorResponse("INVALID_ACTIVITY_APPROVAL_STATE", "Activity could not been approved")
        )
    )

    @ParameterizedTest
    @MethodSource("activityApprovalFailedProvider")
    fun `fail if try to approve an activity and exception is throw`(
        exception: Exception,
        expectedResponseStatus: HttpStatus,
        expectedErrorResponse: ErrorResponse?,
    ) {
        doThrow(exception).whenever(activityApprovalUseCase).approveActivity(ACTIVITY_RESPONSE_DTO.id, Locale.ENGLISH)

        val ex = assertThrows<HttpClientResponseException> {
            client.exchangeObject<Unit>(
                POST("/api/activity/${ACTIVITY_RESPONSE_DTO.id}/approve", "").header(HttpHeaders.ACCEPT_LANGUAGE, "en"),
            )
        }

        assertEquals(expectedResponseStatus, ex.status)
        assertEquals(expectedErrorResponse, ex.response.getBody<ErrorResponse>().get())
    }


    private companion object {
        private val START_DATE = LocalDateTime.of(2018, JANUARY, 10, 8, 0)
        private val END_DATE = LocalDateTime.of(2018, JANUARY, 10, 12, 0)

        private val INTERVAL_REQUEST_DTO = TimeIntervalRequest(
            START_DATE, END_DATE
        )

        private val ACTIVITY_REQUEST_BODY_DTO = ActivityRequest(
            null, INTERVAL_REQUEST_DTO, "Activity description", true, 3, arrayListOf()
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
                "evidences": []
            }
        """.trimIndent()

        private val ACTIVITY_WITH_EVIDENCE_POST_JSON = """
            {
                "interval": {
                    "start": "${ACTIVITY_REQUEST_BODY_DTO.interval.start.toJson()}",
                    "end": "${ACTIVITY_REQUEST_BODY_DTO.interval.end.toJson()}"
                },                
                "description": "${ACTIVITY_REQUEST_BODY_DTO.description}",
                "billable": ${ACTIVITY_REQUEST_BODY_DTO.billable},
                "projectRoleId": ${ACTIVITY_REQUEST_BODY_DTO.projectRoleId},
                "evidences": ["b4afdac6-e536-41de-8a44-2561f8ffad50"]
            }
        """.trimIndent()

        private val ACTIVITY_WITH_WRONG_EVIDENCE_POST_JSON = """
            {
                "interval": {
                    "start": "${ACTIVITY_REQUEST_BODY_DTO.interval.start.toJson()}",
                    "end": "${ACTIVITY_REQUEST_BODY_DTO.interval.end.toJson()}"
                },                
                "description": "${ACTIVITY_REQUEST_BODY_DTO.description}",
                "billable": ${ACTIVITY_REQUEST_BODY_DTO.billable},
                "projectRoleId": ${ACTIVITY_REQUEST_BODY_DTO.projectRoleId},
                "hasEvidences": true,
                "evidence": "VBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg=="
            }
        """.trimIndent()


        private val ACTIVITY_RESPONSE_DTO = ActivityResponseDTO(
            ACTIVITY_REQUEST_BODY_DTO.billable,
            ACTIVITY_REQUEST_BODY_DTO.description,
            ACTIVITY_REQUEST_BODY_DTO.hasEvidences(),
            2L,
            ACTIVITY_REQUEST_BODY_DTO.projectRoleId,
            IntervalResponseDTO(
                ACTIVITY_REQUEST_BODY_DTO.interval.start, ACTIVITY_REQUEST_BODY_DTO.interval.end, 240, TimeUnit.MINUTES
            ),
            42,
            ApprovalDTO(ApprovalState.ACCEPTED)
        )
        private val ACTIVITY_RESPONSE = ActivityResponse.from(ACTIVITY_RESPONSE_DTO)

        private val ACTIVITY_SUMMARY_DTO = ActivitySummaryDTO(
            START_DATE.toLocalDate(),
            BigDecimal.TEN
        )
        private val ACTIVITY_SUMMARY_RESPONSE = ActivitySummaryResponse.from(ACTIVITY_SUMMARY_DTO)

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
                "evidences": []
            }
        """.trimIndent()

        private const val ACTIVITY_IMAGE =
            "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVQYV2NgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII="
    }

}
