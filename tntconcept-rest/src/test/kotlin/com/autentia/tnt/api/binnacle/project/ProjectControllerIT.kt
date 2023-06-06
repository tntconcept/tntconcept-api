package com.autentia.tnt.api.binnacle.project

import com.autentia.tnt.api.binnacle.ErrorResponse
import com.autentia.tnt.api.binnacle.exchangeList
import com.autentia.tnt.api.binnacle.exchangeObject
import com.autentia.tnt.api.binnacle.getBody
import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.entities.dto.ProjectFilterDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectResponseDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleUserDTO
import com.autentia.tnt.binnacle.exception.ProjectNotFoundException
import com.autentia.tnt.binnacle.usecases.BlockProjectByIdUseCase
import com.autentia.tnt.binnacle.usecases.ProjectByFilterUseCase
import com.autentia.tnt.binnacle.usecases.ProjectByIdUseCase
import com.autentia.tnt.binnacle.usecases.ProjectRoleByProjectIdUseCase
import com.autentia.tnt.binnacle.usecases.UnblockProjectByIdUseCase
import io.micronaut.http.HttpRequest.GET
import io.micronaut.http.HttpRequest.POST
import io.micronaut.http.HttpStatus
import io.micronaut.http.HttpStatus.NOT_FOUND
import io.micronaut.http.HttpStatus.OK
import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.assertThrows
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
        )

        doReturn(projectRequestBody).whenever(projectByIdUseCase).get(projectRequestBody.id)

        val response = client.exchangeObject<ProjectResponseDTO>(GET("/api/project/${projectRequestBody.id}"))

        assertEquals(OK, response.status)
        assertEquals(projectRequestBody, response.body.get())

    }

    @Test
    fun `return all project roles by project id`() {

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

        doReturn(listOf(projectRoleUser)).whenever(projectRoleByProjectIdUseCase).get(projectId)

        val response = client.exchangeList<ProjectRoleUserDTO>(GET("/api/project/$projectId/role"))

        assertEquals(OK, response.status)
        assertEquals(listOf(projectRoleUser), response.body.get())
    }

    @Test
    fun `block project by id`() {
        val projectId = 1L
        val blockProjectRequest = BlockProjectRequest(blockDate = LocalDate.of(2023, 5, 5))

        val response = client.exchangeObject<Any>(POST("/api/project/$projectId/block", blockProjectRequest))

        verify(blockProjectByIdUseCase).blockProject(projectId, blockProjectRequest.blockDate)
        assertThat(response.status).isEqualTo(OK)
    }

    @Test
    fun `unblock project by id`() {
        val projectId = 1L
        val projectDTO = ProjectResponseDTO(
            id = projectId,
            name = "Test project",
            open = true,
            billable = true,
            organizationId = 1
        )
        whenever(unblockProjectByIdUseCase.unblockProject(projectId)).thenReturn(projectDTO)
        val response = client.exchangeObject<Any>(POST("/api/project/$projectId/unblock", ""))

        assertThat(response.status).isEqualTo(OK)
        assertThat(response.body).isEqualTo(projectDTO)
    }

    @ParameterizedTest
    @MethodSource("getFailProvider")
    fun `FAIL when the project to retrieve is not found in the database and exception is throw`(
        exception: Exception,
        expectedResponseStatus: HttpStatus,
        expectedErrorCode: String
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
