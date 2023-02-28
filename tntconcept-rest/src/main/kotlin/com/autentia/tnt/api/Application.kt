package com.autentia.tnt.api

import com.autentia.tnt.api.OpenApiTag.Companion.SECURITY
import io.micronaut.openapi.annotation.OpenAPIInclude
import io.micronaut.runtime.Micronaut
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.servers.Server
import io.swagger.v3.oas.annotations.tags.Tag
import java.time.ZoneOffset
import java.util.TimeZone

@OpenAPIDefinition(
    info = Info(
        title = "tntconcept API",
        version = "0.0.1"
    ),
    tags = [
        Tag(name = SECURITY, description = "Security related endpoints."),
    ],
    servers = [Server(url = "http://localhost:8080/")]
)
@OpenAPIInclude(
    classes = [
        io.micronaut.security.endpoints.LoginController::class,
        io.micronaut.security.endpoints.LogoutController::class,
        io.micronaut.security.token.jwt.endpoints.OauthController::class
    ],
    tags = [Tag(name = SECURITY)]
)
object Application {

    @JvmStatic
    fun main(args: Array<String>) {
        ensureJVMtimeZoneIsUTC()

        Micronaut.build()
            .args(*args)
            .packages("com.autentia.tnt.api")
            .start()
    }

    private fun ensureJVMtimeZoneIsUTC() = TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC))

}
