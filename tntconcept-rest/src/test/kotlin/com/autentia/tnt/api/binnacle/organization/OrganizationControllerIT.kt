package com.autentia.tnt.api.binnacle.organization

import com.autentia.tnt.api.binnacle.exchangeList
import com.autentia.tnt.binnacle.core.domain.OrganizationType
import com.autentia.tnt.binnacle.entities.dto.OrganizationFilterDTO
import com.autentia.tnt.binnacle.entities.dto.OrganizationResponseDTO
import com.autentia.tnt.binnacle.usecases.OrganizationsByFilterUseCase
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

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class OrganizationControllerIT {

    @Inject
    @field:Client("/")
    private lateinit var httpClient: HttpClient

    private lateinit var client: BlockingHttpClient

    @get:MockBean(OrganizationsByFilterUseCase::class)
    internal val organizationsByFilterUseCase = mock<OrganizationsByFilterUseCase>()

    @AfterEach
    fun resetMocks() {
        reset(
            organizationsByFilterUseCase
        )
    }

    @BeforeAll
    fun setUp() {
        client = httpClient.toBlocking()
    }

    @Test
    fun `return all organizations`() {
        val organization = OrganizationResponseDTO(1, "Dummy Organization")
        val organizationFilter = OrganizationFilterDTO(listOf(), null)

        doReturn(listOf(organization)).whenever(organizationsByFilterUseCase).get(organizationFilter)

        val response = client.exchangeList<OrganizationResponse>(HttpRequest.GET("/api/organization"))

        assertEquals(HttpStatus.OK, response.status)
        assertNotNull(response.body())
        assertEquals(1, response.body().count())
        assertEquals(organization.id, response.body().get(0).id)
        assertEquals(organization.name, response.body().get(0).name)
    }

    @Test
    fun `return all organizations by type`() {
        val organization = OrganizationResponseDTO(1, "Dummy Organization")
        val organizationsType = listOf(OrganizationType.CLIENT, OrganizationType.PROVIDER)
        val organizationFilter = OrganizationFilterDTO(organizationsType, null)

        doReturn(listOf(organization)).whenever(organizationsByFilterUseCase).get(organizationFilter)

        val response =
            client.exchangeList<OrganizationResponse>(HttpRequest.GET("/api/organization?types=CLIENT,PROVIDER"))

        assertEquals(HttpStatus.OK, response.status)
        assertNotNull(response.body())
        assertEquals(1, response.body().count())
        assertEquals(organization.id, response.body().get(0).id)
        assertEquals(organization.name, response.body().get(0).name)
    }

}
