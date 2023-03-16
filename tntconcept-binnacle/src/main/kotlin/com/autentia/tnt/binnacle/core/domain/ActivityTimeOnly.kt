package com.autentia.tnt.binnacle.core.domain

import io.micronaut.core.annotation.Introspected
import java.time.LocalDateTime

@Introspected
data class ActivityTimeOnly(
    val start: LocalDateTime,
    val end: LocalDateTime,
    val duration: Int,
    val projectRoleId: Long,
)