package com.autentia.tnt.binnacle.entities.dto


import io.micronaut.core.annotation.Introspected
import java.time.YearMonth

@Introspected
data class SubcontractedActivityRequestDTO (
        val id: Long? = null,
        val month: YearMonth,
        val duration: Int,
        val description: String,
        val projectRoleId: Long,
)