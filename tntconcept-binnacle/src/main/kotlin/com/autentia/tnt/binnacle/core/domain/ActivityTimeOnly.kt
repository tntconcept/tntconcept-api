package com.autentia.tnt.binnacle.core.domain

import java.time.LocalDateTime
@Deprecated("Used in the deprecated Activities Controller")
data class ActivityTimeOnly (
    val startDate: LocalDateTime,
    val duration: Int,
    val projectRoleId: Long
)