package com.autentia.tnt.api.binnacle.project

import io.micronaut.core.annotation.Introspected
import java.time.LocalDate

@Introspected
data class BlockProjectRequestDTO(
    val blockDate: LocalDate
)