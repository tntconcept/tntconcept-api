package com.autentia.tnt.api.binnacle.version

import com.autentia.tnt.api.binnacle.exchangeObject
import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS

@MicronautTest
@TestInstance(PER_CLASS)
@Property(name = "app.version", value = "1.0")
internal class VersionControllerIT {

    private val version = "1.0"

    @Inject
    @field:Client("/")
    lateinit var httpClient: HttpClient

    private lateinit var client: BlockingHttpClient

    @BeforeAll
    fun setup() {
        client = httpClient.toBlocking()
    }

    @Test
    fun `return the application version that the controller got in the parameters`() {

        val response = client.exchangeObject<String>(HttpRequest.GET("/api/version"))

        assertEquals(HttpStatus.OK.code, response.status.code)
        assertEquals(version, response.body.get())

    }

}

