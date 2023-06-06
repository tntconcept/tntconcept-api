package com.autentia.tnt.api.binnacle.project

import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Introspected
@Schema(description = "A request to block a project")
data class BlockProjectRequest(
    @get:Schema(
        description = "Date the project will be blocked until",
        example = "2023-01-01",
        required = true
    )
    val blockDate: LocalDate
)