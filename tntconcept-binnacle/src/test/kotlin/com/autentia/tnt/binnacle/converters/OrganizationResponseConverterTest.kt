package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.dto.OrganizationResponseDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class OrganizationResponseConverterTest {

    private val sut = OrganizationResponseConverter()

    @Test
    fun `given domain Organization should return OrganizationResponseDTO with converted values`() {
        val organization = Organization(2L, "organization", listOf())

        val result = sut.toOrganizationResponseDTO(organization)

        assertEquals(OrganizationResponseDTO(2L, "organization"), result)
    }

}
