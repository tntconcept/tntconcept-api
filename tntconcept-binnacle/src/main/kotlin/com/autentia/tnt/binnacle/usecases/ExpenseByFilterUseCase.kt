package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ExpenseResponseConverter
import com.autentia.tnt.binnacle.entities.Expense
import com.autentia.tnt.binnacle.entities.dto.ExpenseFilterDTO
import com.autentia.tnt.binnacle.entities.dto.ExpenseResponseDTO
import com.autentia.tnt.binnacle.repositories.ExpenseRepository
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
class ExpenseByFilterUseCase internal constructor(
    private val expenseRepository: ExpenseRepository,
    private val expenseResponseConverter: ExpenseResponseConverter
) {

    @Transactional
    @ReadOnly
    fun getExpenses(expenseFilter: ExpenseFilterDTO): List<ExpenseResponseDTO> {
        val expenses = filterExpenses(expenseFilter)
        return expenses.map { expenseResponseConverter.toResponseDTO(it) }
    }

    private fun filterExpenses(expenseFilter: ExpenseFilterDTO): List<Expense> {
        return when {
            expenseFilter.startDate != null && expenseFilter.endDate != null -> {
                if (expenseFilter.state != null) {
                    expenseRepository.find(expenseFilter.startDate, expenseFilter.endDate, expenseFilter.state!!)
                } else {
                    expenseRepository.find(expenseFilter.startDate, expenseFilter.endDate)
                }
            }

            expenseFilter.state != null -> {
                expenseRepository.find(expenseFilter.state!!)
            }

            else -> {
                throw IllegalArgumentException("At least one filter must be provided.")
            }
        }
    }

}
