package com.autentia.tnt.binnacle.entities.dto

import java.math.BigDecimal
import java.time.LocalDateTime

data class ExpenseResponseDTO (
    val date: LocalDateTime,
    val description: String,
    val amount: BigDecimal,
    val hasAttachments: Boolean,
    val state: String
)
