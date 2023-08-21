package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.entities.Expense
import com.autentia.tnt.binnacle.entities.dto.ExpenseResponseDTO
import jakarta.inject.Singleton

@Singleton
class ExpenseResponseConverter {
    fun toResponseDTO(expense: Expense) = ExpenseResponseDTO(
        id= expense.id!!,
        userId = expense.userId,
        date = expense.date,
        description = expense.description,
        amount = expense.amount,
        hasAttachments = expense.attachments.isNotEmpty(),
        state = expense.state.name
    )
}
