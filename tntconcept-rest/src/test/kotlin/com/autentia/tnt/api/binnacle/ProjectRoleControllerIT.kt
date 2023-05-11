package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleUserDTO
import com.autentia.tnt.binnacle.exception.ProjectRoleNotFoundException
import com.autentia.tnt.binnacle.usecases.LatestProjectRolesForAuthenticatedUserUseCase
import com.autentia.tnt.binnacle.usecases.ProjectRoleByIdUseCase
import com.autentia.tnt.binnacle.usecases.ProjectRoleByUserIdsUseCase
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
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@MicronautTest
@TestInstance(PER_CLASS)
internal class ProjectRoleControllerIT {

    @Inject
    @field:Client("/")
    private lateinit var httpClient: HttpClient

    private lateinit var client: BlockingHttpClient

    @get:MockBean(ProjectRoleByIdUseCase::class)
    internal val projectRoleByIdUseCase = mock<ProjectRoleByIdUseCase>()

    @get:MockBean(ProjectRoleByUserIdsUseCase::class)
    internal val projectRoleByUserIdsUseCase = mock<ProjectRoleByUserIdsUseCase>()

    @get:MockBean(LatestProjectRolesForAuthenticatedUserUseCase::class)
    internal val latestProjectRolesForAuthenticatedUserUseCase = mock<LatestProjectRolesForAuthenticatedUserUseCase>()


    @BeforeAll
    fun setUp() {
        client = httpClient.toBlocking()
    }

    @Test
    fun `get the project roles`() {

        val projectId = 1

        val projectRole = ProjectRoleDTO(
            1L,
            "Asistente",
            1L,
            1L,
            10,
            true,
            TimeUnit.MINUTES,
            RequireEvidence.WEEKLY,
            true
        )

        doReturn(projectRole).whenever(projectRoleByIdUseCase).get(projectRole.id)


        val response = client.exchangeObject<ProjectRoleDTO>(GET("/api/project-role/$projectId"))

        assertEquals(OK, response.status)
        assertEquals(projectRole, response.body.get())

    }

    @ParameterizedTest
    @MethodSource("getFailProvider")
    fun `return 404 if the project role does not exist`(
        exception: Exception,
        expectedResponseStatus: HttpStatus,
        expectedErrorCode: String
    ) {
        val projectId = 1L

        doThrow(exception).whenever(projectRoleByIdUseCase).get(projectId)

        val ex = assertThrows<HttpClientResponseException> {
            client.exchangeObject<Any>(GET("/api/project-role/$projectId"))
        }

        assertEquals(expectedResponseStatus, ex.status)
        assertEquals(expectedErrorCode, ex.response.getBody<ErrorResponse>().get().code)

    }

    @Test
    fun `get the recent project roles`() {

        val projectRoleUserDTO = ProjectRoleUserDTO(
            1L,
            "desarrollador",
            1L,
            1L,
            10,
            0,
            TimeUnit.MINUTES,
            RequireEvidence.WEEKLY,
            true,
            1L
        )

        doReturn(listOf(projectRoleUserDTO)).whenever(latestProjectRolesForAuthenticatedUserUseCase).get()

        val response = client.exchangeList<ProjectRoleUserDTO>(GET("/api/project-role/latest"))

        assertEquals(OK, response.status)
        assertEquals(listOf(projectRoleUserDTO), response.body.get())

    }

    @Test
    fun `get the project roles of a list of user Ids`() {

        val userIds = listOf<Long>(1,2)

        val projectRoleResponse = listOf<ProjectRoleUserDTO>()

        doReturn(projectRoleResponse).whenever(projectRoleByUserIdsUseCase).get(userIds)

        val response = client.exchangeList<ProjectRoleUserDTO>(GET("/api/project-role?userIds=1,2"))

        assertEquals(OK, response.status)
        assertEquals(projectRoleResponse, response.body.get())
    }

    private fun getFailProvider() = arrayOf(
        arrayOf(ProjectRoleNotFoundException(1), NOT_FOUND, "RESOURCE_NOT_FOUND"),
    )

}
