package com.autentia.tnt.api.binnacle.search

import com.autentia.tnt.api.binnacle.exchangeObject
import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.entities.dto.OrganizationResponseDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectResponseDTO
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleUserDTO
import com.autentia.tnt.binnacle.entities.dto.SearchResponseDTO
import com.autentia.tnt.binnacle.usecases.SearchByRoleIdUseCase
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import java.time.LocalDate

@MicronautTest
@TestInstance(PER_CLASS)
internal class SearchControllerIT {

    @Inject
    @field:Client("/")
    private lateinit var httpClient: HttpClient

    private lateinit var client: BlockingHttpClient

    @get:MockBean(SearchByRoleIdUseCase::class)
    internal val searchUseCase = org.mockito.kotlin.mock<SearchByRoleIdUseCase>()

    @BeforeAll
    fun setUp() {
        client = httpClient.toBlocking()
    }

    @Test
    fun `return roles, project and organization for one unique roleid with year `() {

        val searchedRoles = listOf(TRAINING.id)
        val roleDescriptions = SearchResponseDTO(
                listOf(AUTENTIA),
                listOf(TRAINING),
                listOf(STUDENT)
        )
        doReturn(roleDescriptions).whenever(searchUseCase).get(searchedRoles, YEAR)

        val response = client.exchangeObject<SearchResponse>(HttpRequest.GET("/api/search?roleIds=${TRAINING.id}&year=$YEAR"))

        assertEquals(HttpStatus.OK, response.status)
        assertEquals(SearchResponse.from(roleDescriptions), response.body.get())

    }


    @Test
    fun `return role, project and organization for one unique roleid without year `() {

        val searchedRoles = listOf(TRAINING.id)
        val roleDescriptions = SearchResponseDTO(
                listOf(AUTENTIA),
                listOf(TRAINING),
                listOf(STUDENT)
        )
        doReturn(roleDescriptions).whenever(searchUseCase).get(searchedRoles, null)

        val response = client.exchangeObject<SearchResponse>(HttpRequest.GET("/api/search?roleIds=${TRAINING.id}"))

        assertEquals(HttpStatus.OK, response.status)
        assertEquals(SearchResponse.from(roleDescriptions), response.body.get())


    }


    private companion object {

        private const val YEAR = 2023

        private val AUTENTIA = OrganizationResponseDTO(1, "Autentia")
        private val TRAINING = ProjectResponseDTO(1, "Formación Autentia", true, false, AUTENTIA.id, LocalDate.now())
        private val STUDENT = ProjectRoleUserDTO(
                1,
                "Alumno en formación",
                AUTENTIA.id,
                TRAINING.id,
                1440,
                60,
                TimeUnit.MINUTES,
                RequireEvidence.WEEKLY,
                false,
                1L
        )
    }

}
