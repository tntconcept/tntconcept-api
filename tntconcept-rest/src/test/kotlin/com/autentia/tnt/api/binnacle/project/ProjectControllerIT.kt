package com.autentia.tnt.api.binnacle.project

import com.autentia.tnt.api.binnacle.*
import com.autentia.tnt.api.binnacle.projectrole.MaxTimeAllowedResponse
import com.autentia.tnt.api.binnacle.projectrole.ProjectRoleUserResponse
import com.autentia.tnt.api.binnacle.projectrole.TimeInfoResponse
import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.entities.dto.*
import com.autentia.tnt.binnacle.exception.InvalidBlockDateException
import com.autentia.tnt.binnacle.exception.ProjectClosedException
import com.autentia.tnt.binnacle.exception.ProjectNotFoundException
import com.autentia.tnt.binnacle.usecases.*
import io.micronaut.http.HttpRequest.GET
import io.micronaut.http.HttpRequest.POST
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
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.*
import java.time.LocalDate

@MicronautTest
@TestInstance(PER_CLASS)
internal class ProjectControllerIT {

    @Inject
    @field:Client("/")
    private lateinit var httpClient: HttpClient

    private lateinit var client: BlockingHttpClient

    @get:MockBean(ProjectByIdUseCase::class)
    internal val projectByIdUseCase = mock<ProjectByIdUseCase>()

    @get:MockBean(ProjectRoleByProjectIdUseCase::class)
    internal val projectRoleByProjectIdUseCase = mock<ProjectRoleByProjectIdUseCase>()

    @get:MockBean(BlockProjectByIdUseCase::class)
    internal val blockProjectByIdUseCase = mock<BlockProjectByIdUseCase>()

    @get:MockBean(UnblockProjectByIdUseCase::class)
    internal val unblockProjectByIdUseCase = mock<UnblockProjectByIdUseCase>()

    @get:MockBean(ProjectByFilterUseCase::class)
    internal val projectByFilterUseCase = mock<ProjectByFilterUseCase>()


    @AfterEach
    fun resetMocks() {
        reset(
            projectByIdUseCase,
            projectRoleByProjectIdUseCase,
            blockProjectByIdUseCase,
            unblockProjectByIdUseCase,
            projectByFilterUseCase

        )
    }

    @BeforeAll
    fun setUp() {
        client = httpClient.toBlocking()
    }

    @Test
    fun `return the correct project`() {
        // setup
        val projectRequestBody = PROJECT_RESPONSE_DTO

        doReturn(projectRequestBody).whenever(projectByIdUseCase).get(projectRequestBody.id)

        val response = client.exchangeObject<ProjectResponse>(GET("/api/project/${projectRequestBody.id}"))

        assertEquals(OK, response.status)
        assertEquals(PROJECT_RESPONSE, response.body.get())

    }

    @Test
    fun `return all project roles by project id of requested year`() {

        val projectId = 3L
        val year = 2023

        doReturn(listOf(PROJECT_ROLE_USER_DTO)).whenever(projectRoleByProjectIdUseCase).get(projectId, year, null)

        val response = client.exchangeList<ProjectRoleUserResponse>(GET("/api/project/$projectId/role?year=$year"))

        assertEquals(OK, response.status)
        assertEquals(listOf(PROJECT_ROLE_USER_RESPONSE), response.body.get())
    }

    @Test
    fun `return all project roles by project id of requested user`() {

        val projectId = 3L
        val year = 2023
        val userId = 2L

        doReturn(listOf(PROJECT_ROLE_USER_DTO)).whenever(projectRoleByProjectIdUseCase).get(projectId, year, userId)

        val response = client.exchangeList<ProjectRoleUserResponse>(GET("/api/project/$projectId/role?year=$year&userId=$userId"))

        assertEquals(OK, response.status)
        assertEquals(listOf(PROJECT_ROLE_USER_RESPONSE), response.body.get())
    }

    @Test
    fun `return all project roles by project id without requested year`() {

        val projectId = 3L

        doReturn(listOf(PROJECT_ROLE_USER_DTO)).whenever(projectRoleByProjectIdUseCase).get(projectId, null, null)

        val response = client.exchangeList<ProjectRoleUserResponse>(GET("/api/project/$projectId/role"))

        assertEquals(OK, response.status)
        assertEquals(listOf(PROJECT_ROLE_USER_RESPONSE), response.body.get())
    }


    private fun putFailProvider() = arrayOf(
        arrayOf(InvalidBlockDateException(), BAD_REQUEST, "INVALID_BLOCK_DATE"),
        arrayOf(ProjectClosedException(), BAD_REQUEST, "CLOSED_PROJECT"),
    )

    @ParameterizedTest
    @MethodSource("putFailProvider")
    fun `fail to block project and exception is thrown`(
        exception: Exception,
        expectedResponseStatus: HttpStatus,
        expectedErrorCode: String,
    ) {

        val projectId = 1L
        val blockProjectRequest = BlockProjectRequest(blockDate = LocalDate.of(2023, 5, 5))

        doThrow(exception).whenever(blockProjectByIdUseCase).blockProject(projectId, blockProjectRequest.blockDate)

        val ex = assertThrows<HttpClientResponseException> {
            client.exchangeObject<Any>(
                POST("/api/project/$projectId/block", blockProjectRequest)
            )
        }

        assertEquals(expectedResponseStatus, ex.status)
        assertEquals(expectedErrorCode, ex.response.getBody<ErrorResponse>().get().code)
    }


    @Test
    fun `fail to unblock project and exception is thrown`() {

        val projectId = 1L

        doThrow(ProjectClosedException()).whenever(unblockProjectByIdUseCase).unblockProject(projectId)

        val ex = assertThrows<HttpClientResponseException> {
            client.exchangeObject<Any>(
                POST("/api/project/$projectId/unblock", "")
            )
        }

        assertEquals(BAD_REQUEST, ex.status)
        assertEquals("CLOSED_PROJECT", ex.response.getBody<ErrorResponse>().get().code)
    }

    @Test
    fun `block project by id`() {
        val projectId = 1L
        val blockProjectRequest = BlockProjectRequest(blockDate = LocalDate.of(2023, 5, 5))
        val projectResponseDTO = PROJECT_RESPONSE_DTO
        whenever(blockProjectByIdUseCase.blockProject(projectId, blockProjectRequest.blockDate)).thenReturn(
            projectResponseDTO
        )

        val response =
            client.exchangeObject<ProjectResponse>(POST("/api/project/$projectId/block", blockProjectRequest))

        assertThat(response.status).isEqualTo(OK)
        assertThat(response.getBody<ProjectResponse>().get()).isEqualTo(PROJECT_RESPONSE)
    }

    @Test
    fun `unblock project by id`() {
        val projectId = 1L
        val projectResponseDTO = PROJECT_RESPONSE_DTO
        whenever(unblockProjectByIdUseCase.unblockProject(projectId)).thenReturn(projectResponseDTO)
        val response = client.exchangeObject<ProjectResponse>(POST("/api/project/$projectId/unblock", ""))

        assertThat(response.status).isEqualTo(OK)
        assertThat(response.getBody<ProjectResponse>().get()).isEqualTo(PROJECT_RESPONSE)
    }

    @ParameterizedTest
    @MethodSource("getFailProvider")
    fun `FAIL when the project to retrieve is not found in the database and exception is thrown`(
        exception: Exception,
        expectedResponseStatus: HttpStatus,
        expectedErrorCode: String,
    ) {

        val projectId = 1L

        doThrow(exception).whenever(projectByIdUseCase).get(projectId)

        val ex = assertThrows<HttpClientResponseException> {
            client.exchangeObject<Any>(GET("/api/project/$projectId"))
        }

        assertEquals(expectedResponseStatus, ex.status)
        assertEquals(expectedErrorCode, ex.response.getBody<ErrorResponse>().get().code)

    }

    @Test
    fun `return all filtered projects`() {
        val projectRequestBody = PROJECT_RESPONSE_DTO

        val projectFilter = ProjectFilterDTO(1, false)
        whenever(projectByFilterUseCase.getProjects(projectFilter)).thenReturn(listOf(projectRequestBody))

        val response = client.exchangeList<ProjectResponse>(GET("/api/project?organizationId=1&open=false"))

        assertEquals(OK, response.status)
        assertEquals(listOf(PROJECT_RESPONSE), response.body())
    }

    private fun getFailProvider() = arrayOf(
        arrayOf(ProjectNotFoundException(1), NOT_FOUND, "RESOURCE_NOT_FOUND"),
    )

    private companion object {

        private val PROJECT_RESPONSE = ProjectResponse(
            1L,
            "Dummy Project",
            false,
            false,
            1L,
            startDate = LocalDate.now(),
        )

        private val MAX_TIME_ALLOWED_RESPONSE = MaxTimeAllowedResponse(
            960, 0
        )

        private val REMAINING_TIME_INFO_RESPONSE = TimeInfoResponse(
            MAX_TIME_ALLOWED_RESPONSE, TimeUnit.MINUTES, 480
        )

        private val PROJECT_ROLE_USER_RESPONSE = ProjectRoleUserResponse(
            1L,
            "Vacaciones",
            2L,
            3L,
            REMAINING_TIME_INFO_RESPONSE,
            RequireEvidence.NO,
            true,
            4L
        )

        private val PROJECT_ROLE_USER_DTO = ProjectRoleUserDTO(
            1L,
            "Vacaciones",
            2L,
            3L,
            RequireEvidence.NO,
            true,
            4L,
            TimeInfoDTO(MaxTimeAllowedDTO(960,0), TimeUnit.MINUTES, 480)
        )

        private val PROJECT_RESPONSE_DTO = ProjectResponseDTO(
            1L,
            "Dummy Project",
            false,
            false,
            1L,
            LocalDate.now(),
        )
    }
}
