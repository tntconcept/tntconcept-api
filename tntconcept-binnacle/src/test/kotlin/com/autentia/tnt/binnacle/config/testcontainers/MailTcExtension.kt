package com.autentia.tnt.binnacle.config.testcontainers

import org.junit.jupiter.api.extension.Extension
import org.testcontainers.containers.GenericContainer

internal object MailTcExtension : Extension {
    private const val SMTP_PORT = 1025
    private const val HTTP_PORT = 1080
    private val container = GenericContainer("reachfive/fake-smtp-server:latest")
        .withExposedPorts(SMTP_PORT, HTTP_PORT)

    init {
        container.start()
        System.setProperty("spring.mail.host", container.host)
        System.setProperty("spring.mail.port", container.getMappedPort(SMTP_PORT).toString())
        System.setProperty("spring.mail.http.interface", container.getMappedPort(HTTP_PORT).toString())
    }
}
