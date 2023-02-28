package com.autentia.tnt.api.binnacle

import io.micronaut.context.annotation.Value
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.Operation

@Controller("/api/version")

@Validated
internal class VersionController(
    @Value("\${app.version}") private val version: String
) {

    @Operation(summary = "Retrieves api version", description = "200 expected")
    @Get
    fun getVersion(): String = version

}
