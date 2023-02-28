package com.autentia.tnt.binnacle.core.domain

import java.time.LocalDateTime

data class ActivityTimeOnly (
    val startDate: LocalDateTime,
    val duration: Int,
    val projectRoleId: Long
)
