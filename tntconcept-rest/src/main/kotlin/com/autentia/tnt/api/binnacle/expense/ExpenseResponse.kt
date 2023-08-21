package com.autentia.tnt.api.binnacle.expense

import com.autentia.tnt.binnacle.entities.dto.ExpenseResponseDTO
import java.math.BigDecimal
import java.time.LocalDateTime

data class ExpenseResponse(
    val id: Long,
    val userId: Long,
    val date: LocalDateTime,
    val description: String,
    val amount: BigDecimal,
    val hasAttachments: Boolean,
    val state: String
) {
    companion object {
        fun from(expenseResponseDTO: ExpenseResponseDTO) = ExpenseResponse(
            expenseResponseDTO.id,
            expenseResponseDTO.userId,
            expenseResponseDTO.date,
            expenseResponseDTO.description,
            expenseResponseDTO.amount,
            expenseResponseDTO.hasAttachments,
            expenseResponseDTO.state
        )
    }
}