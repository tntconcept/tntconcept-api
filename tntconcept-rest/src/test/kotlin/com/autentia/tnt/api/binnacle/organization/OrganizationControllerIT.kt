package com.autentia.tnt.api.binnacle.organization

import com.autentia.tnt.api.binnacle.createOrganization
import com.autentia.tnt.api.binnacle.exchangeList
import com.autentia.tnt.api.binnacle.project.ProjectResponse
import com.autentia.tnt.binnacle.core.domain.OrganizationType
import com.autentia.tnt.binnacle.entities.dto.OrganizationResponseDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectResponseDTO
import com.autentia.tnt.binnacle.usecases.ImputableOrganizationsUseCase
import com.autentia.tnt.binnacle.usecases.ImputableProjectsByOrganizationIdUseCase
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import java.time.LocalDate

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class OrganizationControllerIT {

    @Inject
    @field:Client("/")
    private lateinit var httpClient: HttpClient

    private lateinit var client: BlockingHttpClient

    @get:MockBean(ImputableOrganizationsUseCase::class)
    internal val imputableOrganizationsUseCase = mock<ImputableOrganizationsUseCase>()

    @get:MockBean(ImputableProjectsByOrganizationIdUseCase::class)
    internal val imputableProjectsByOrganizationIdUseCase = mock<ImputableProjectsByOrganizationIdUseCase>()

    @AfterEach
    fun resetMocks() {
        reset(
            imputableOrganizationsUseCase,
            imputableProjectsByOrganizationIdUseCase
        )
    }

    @BeforeAll
    fun setUp() {
        client = httpClient.toBlocking()
    }

    @Test
    fun `return all imputable organizations`() {
        val organization = OrganizationResponseDTO(1, "Dummy Organization")

        doReturn(listOf(organization)).whenever(imputableOrganizationsUseCase).get(null)

        val response = client.exchangeList<OrganizationResponse>(HttpRequest.GET("/api/organizations"))

        assertEquals(HttpStatus.OK, response.status)
        assertNotNull(response.body())
        assertEquals(1, response.body().count())
        assertEquals(organization.id, response.body().get(0).id)
        assertEquals(organization.name, response.body().get(0).name)
    }

    @Test
    fun `return all organizations by type`() {
        val organization = OrganizationResponseDTO(1, "Dummy Organization")
        val organizationType = OrganizationType.CLIENT

        doReturn(listOf(organization)).whenever(imputableOrganizationsUseCase).get(organizationType)

        val response = client.exchangeList<OrganizationResponse>(HttpRequest.GET("/api/organizations?type=CLIENT"))

        assertEquals(HttpStatus.OK, response.status)
        assertNotNull(response.body())
        assertEquals(1, response.body().count())
        assertEquals(organization.id, response.body().get(0).id)
        assertEquals(organization.name, response.body().get(0).name)
    }

    @Test
    fun `return all projects by organization`() {
        val organization = createOrganization()
        val project = ProjectResponseDTO(1, "Dummy Project", true, true, 1L, startDate = LocalDate.now())

        doReturn(listOf(project)).whenever(imputableProjectsByOrganizationIdUseCase).get(organization.id)

        val response = client.exchangeList<ProjectResponse>(HttpRequest.GET("/api/organizations/${project.id}/projects"))

        val expectedProjectDTO = ProjectResponse(1, "Dummy Project", true, true, 1L, startDate = LocalDate.now())

        assertEquals(HttpStatus.OK, response.status)
        assertNotNull(response.body())
        assertEquals(listOf(expectedProjectDTO), response.body())
    }

}
