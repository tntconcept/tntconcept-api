package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.entities.dto.ProjectResponseDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleUserDTO
import com.autentia.tnt.binnacle.exception.ProjectNotFoundException
import com.autentia.tnt.binnacle.usecases.ProjectByIdUseCase
import com.autentia.tnt.binnacle.usecases.ProjectRoleByProjectIdUseCase
import io.micronaut.http.HttpRequest.GET
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
import org.junit.jupiter.api.Assertions.*
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
import org.mockito.kotlin.whenever

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
            1L
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

    private fun getFailProvider() = arrayOf(
        arrayOf(ProjectNotFoundException(1), NOT_FOUND, "RESOURCE_NOT_FOUND"),
    )

}
