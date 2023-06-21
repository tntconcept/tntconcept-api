package com.autentia.tnt.binnacle.repositories

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS

@MicronautTest
@TestInstance(PER_CLASS)
internal class OrganizationRepositoryIT {

    @Inject
    private lateinit var organizationRepository: OrganizationRepository

    @Test
    fun `should find the organization by id`() {
        val idToSearch = 1L

        val result = organizationRepository.findById(idToSearch)

        assertEquals(idToSearch, result.get().id)
    }

    @Test
    fun `should find all the organizations`() {
        val result = organizationRepository.findAll()

        assertEquals(3, result.count())
    }

}
