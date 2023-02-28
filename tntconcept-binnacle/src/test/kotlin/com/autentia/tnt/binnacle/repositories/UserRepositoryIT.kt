package com.autentia.tnt.binnacle.repositories

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
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
    fun `should find the user by username`() {
        val username = "admin"

        val result = userRepository.findByUsername(username)

        assertNotNull(result)
        assertEquals(username, result!!.username)
    }

    @Test
    fun `should return null if the username does not exist`() {
        val username = "Whops!"

        val result = userRepository.findByUsername(username)

        assertNull(result)
    }

    @Test
    fun `should find active users`() {
        val result = userRepository.findByActiveTrue()

        assertTrue(result.isNotEmpty())
        result.forEach { assertTrue(it.active) }
    }
}
