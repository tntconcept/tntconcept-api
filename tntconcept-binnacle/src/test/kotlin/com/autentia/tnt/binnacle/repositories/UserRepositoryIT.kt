package com.autentia.tnt.binnacle.repositories

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS

@MicronautTest
@TestInstance(PER_CLASS)
internal class UserRepositoryIT {

    @Inject
    private lateinit var userRepository: UserRepository

    @Test
    fun `should find active users`() {
        val result = userRepository.findByActiveTrue()

        assertTrue(result.isNotEmpty())
        result.forEach { assertTrue(it.active) }
    }
}
