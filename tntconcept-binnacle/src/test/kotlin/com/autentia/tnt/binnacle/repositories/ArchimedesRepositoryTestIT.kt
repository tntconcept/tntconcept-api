package com.autentia.tnt.binnacle.repositories

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ArchimedesRepositoryTestIT {

    @Inject
    private lateinit var archimedesRepository: ArchimedesRepository

    @Test
    fun `subjects that have role user`() {
        val users = archimedesRepository.findAllPrincipalNameByRoleName("user")
        assertThat(users).isNotEmpty()
    }

    @Test
    fun `subjects that have role activity-approval role`() {
        val users = archimedesRepository.findAllPrincipalNameByRoleName("activity-approval")
        assertThat(users).isEmpty()
    }
}