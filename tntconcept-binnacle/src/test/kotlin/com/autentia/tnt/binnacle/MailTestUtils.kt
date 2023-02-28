package com.autentia.tnt.binnacle

import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.uri.UriBuilder
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
internal class MailTestUtils {

    @Inject
    @Client("/")
    var httpClient: HttpClient? = null

    fun getSentEmail(): String {
        val host = System.getProperty("spring.mail.host")
        val port = System.getProperty("spring.mail.http.interface")

        val client = httpClient!!.toBlocking()

        val request: HttpRequest<*> = HttpRequest.GET<Any>(
                UriBuilder.of("http://$host:$port/api/emails").build() )

        return client.retrieve(request, String::class.java)
    }
}
