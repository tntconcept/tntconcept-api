package com.autentia.tnt.api.binnacle.project

import com.autentia.tnt.api.binnacle.*
import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.entities.dto.ProjectFilterDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectResponseDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleUserDTO
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
        val projectRequestBody = ProjectResponseDTO(
                1L,
                "Vacaciones",
                true,
                true,
                1L,
                startDate = LocalDate.now(),
        )

        doReturn(projectRequestBody).whenever(projectByIdUseCase).get(projectRequestBody.id)

        val response = client.exchangeObject<ProjectResponseDTO>(GET("/api/project/${projectRequestBody.id}"))

        assertEquals(OK, response.status)
        assertEquals(projectRequestBody, response.body.get())

    }

    @Test
    fun `return all project roles by project id of requested year`() {

        val projectId = 3L
        val year = 2023

        val projectRoleUser = ProjectRoleUserDTO(
                1L,
                "Vacaciones",
                2L,
                3L,
                960,
                480,
                TimeUnit.MINUTES,
                RequireEvidence.NO,
                true,
                4L
        )

        doReturn(listOf(projectRoleUser)).whenever(projectRoleByProjectIdUseCase).get(projectId, year)

        val response = client.exchangeList<ProjectRoleUserDTO>(GET("/api/project/$projectId/role?year=$year"))

        assertEquals(OK, response.status)
        assertEquals(listOf(projectRoleUser), response.body.get())
    }

    @Test
    fun `return all project roles by project id without requested year`() {

        val projectId = 3L

        val projectRoleUser = ProjectRoleUserDTO(
                1L,
                "Vacaciones",
                2L,
                3L,
                960,
                480,
                TimeUnit.MINUTES,
                RequireEvidence.NO,
                true,
                4L
        )

        doReturn(listOf(projectRoleUser)).whenever(projectRoleByProjectIdUseCase).get(projectId, null)

        val response = client.exchangeList<ProjectRoleUserDTO>(GET("/api/project/$projectId/role"))

        assertEquals(OK, response.status)
        assertEquals(listOf(projectRoleUser), response.body.get())
    }


    private fun putFailProvider() = arrayOf(
            arrayOf(InvalidBlockDateException(), BAD_REQUEST, "INVALID_BLOCK_DATE"),
            arrayOf(ProjectClosedException(), BAD_REQUEST, "CLOSED_PROJECT"),
    )

    @ParameterizedTest
    @MethodSource("putFailProvider")
    fun `fail to block project and exception is throw`(
            exception: Exception,
            expectedResponseStatus: HttpStatus,
            expectedErrorCode: String,
    ) {

        val projectId = 1L
        val blockProjectRequest = BlockProjectRequestDTO(blockDate = LocalDate.of(2023, 5, 5))

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
    fun `fail to unblock project and exception is throw`() {

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
        val blockProjectRequest = BlockProjectRequestDTO(blockDate = LocalDate.of(2023, 5, 5))
        val projectResponseDTO = createProjectResponseDTO()
        whenever(blockProjectByIdUseCase.blockProject(projectId, blockProjectRequest.blockDate)).thenReturn(
                projectResponseDTO
        )

        val response =
                client.exchangeObject<ProjectResponseDTO>(POST("/api/project/$projectId/block", blockProjectRequest))

        assertThat(response.status).isEqualTo(OK)
        assertThat(response.getBody<ProjectResponseDTO>().get()).isEqualTo(projectResponseDTO)
    }

    @Test
    fun `unblock project by id`() {
        val projectId = 1L
        val projectResponseDTO = createProjectResponseDTO()
        whenever(unblockProjectByIdUseCase.unblockProject(projectId)).thenReturn(projectResponseDTO)
        val response = client.exchangeObject<ProjectResponseDTO>(POST("/api/project/$projectId/unblock", ""))

        assertThat(response.status).isEqualTo(OK)
        assertThat(response.getBody<ProjectResponseDTO>().get()).isEqualTo(projectResponseDTO)
    }

    @ParameterizedTest
    @MethodSource("getFailProvider")
    fun `FAIL when the project to retrieve is not found in the database and exception is throw`(
            exception: Exception,
            expectedResponseStatus: HttpStatus,
            expectedErrorCode: String,
    ) {

        val projectId = 1L

        doThrow(exception).whenever(projectByIdUseCase).get(projectId)

        val ex = assertThrows<HttpClientResponseException> {
            client.exchangeObject<Any>(GET("/api/projects/$projectId"))
        }

        assertEquals(expectedResponseStatus, ex.status)
        assertEquals(expectedErrorCode, ex.response.getBody<ErrorResponse>().get().code)

    }

    @Test
    fun `return all filtered projects`() {
        val projectRequestBody = ProjectResponseDTO(
                1L,
                "Vacaciones",
                true,
                true,
                1L,
                startDate = LocalDate.now().minusMonths(2L),
        )
        val projectFilter = ProjectFilterDTO(1, false)
        whenever(projectByFilterUseCase.getProjects(projectFilter)).thenReturn(listOf(projectRequestBody))

        val response = client.exchangeList<ProjectResponseDTO>(GET("/api/project?organizationId=1&open=false"))

        assertEquals(OK, response.status)
        assertEquals(listOf(projectRequestBody), response.body())
    }

    private fun getFailProvider() = arrayOf(
            arrayOf(ProjectNotFoundException(1), NOT_FOUND, "RESOURCE_NOT_FOUND"),
    )

}
