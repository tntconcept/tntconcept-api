package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ExpenseResponseConverter
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.AttachmentInfo
import com.autentia.tnt.binnacle.entities.Expense
import com.autentia.tnt.binnacle.entities.ExpenseType
import com.autentia.tnt.binnacle.entities.dto.ExpenseFilterDTO
import com.autentia.tnt.binnacle.repositories.ExpenseRepository
import org.junit.Assert
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

internal class ExpenseByFilterUseCaseTest {

    private val expenseRepository: ExpenseRepository = mock<ExpenseRepository>()
    private val expenseResponseConverter = ExpenseResponseConverter()
    private val expenseByFilterUseCase = ExpenseByFilterUseCase(expenseRepository, expenseResponseConverter)

    @Test
    fun `find expenses with all filters return data`() {
        whenever(
            expenseRepository.find(
                LocalDateTime.of(2023, 8, 23, 0, 0, 0),
                LocalDateTime.of(2023, 8, 31, 0, 0, 0),
                ApprovalState.PENDING
            )
        ).thenReturn(listOf(expenseExpected))

        val expenses = expenseByFilterUseCase.getExpenses(filterComplete)

        verify(expenseRepository).find(
            LocalDateTime.of(2023, 8, 23, 0, 0, 0),
            LocalDateTime.of(2023, 8, 31, 0, 0, 0),
            ApprovalState.PENDING
        )

        Assertions.assertEquals(LocalDateTime.of(2023, 8, 23, 0, 0, 0), expenses[0].date)
        Assertions.assertEquals("expense", expenses[0].description)
        Assertions.assertEquals(BigDecimal(10.0), expenses[0].amount)
        Assertions.assertEquals(true, expenses[0].hasAttachments)
        Assertions.assertEquals("PENDING", expenses[0].state)
    }

    @Test
    fun `find expenses with date range filters return data`() {
        whenever(
            expenseRepository.find(
                LocalDateTime.of(2023, 8, 23, 0, 0, 0),
                LocalDateTime.of(2023, 8, 31, 0, 0, 0)
            )
        ).thenReturn(listOf(expenseExpected))

        val expenses = expenseByFilterUseCase.getExpenses(filterDateRange)

        verify(expenseRepository).find(
            LocalDateTime.of(2023, 8, 23, 0, 0, 0),
            LocalDateTime.of(2023, 8, 31, 0, 0, 0)
        )

        Assertions.assertEquals(LocalDateTime.of(2023, 8, 23, 0, 0, 0), expenses[0].date)
        Assertions.assertEquals("expense", expenses[0].description)
        Assertions.assertEquals(BigDecimal(10.0), expenses[0].amount)
        Assertions.assertEquals(true, expenses[0].hasAttachments)
        Assertions.assertEquals("PENDING", expenses[0].state)
    }

    @Test
    fun `find expenses with state filters return data`() {
        whenever(
            expenseRepository.find(
                ApprovalState.PENDING
            )
        ).thenReturn(listOf(expenseExpected))

        val expenses = expenseByFilterUseCase.getExpenses(filterState)

        verify(expenseRepository).find(
            ApprovalState.PENDING
        )

        Assertions.assertEquals(LocalDateTime.of(2023, 8, 23, 0, 0, 0), expenses[0].date)
        Assertions.assertEquals("expense", expenses[0].description)
        Assertions.assertEquals(BigDecimal(10.0), expenses[0].amount)
        Assertions.assertEquals(true, expenses[0].hasAttachments)
        Assertions.assertEquals("PENDING", expenses[0].state)
    }

    @Test
    fun `find expenses without filters throw an exception`() {
        Assert.assertThrows(IllegalArgumentException::class.java) {
            expenseByFilterUseCase.getExpenses(filterEmpty)
        }

        verify(expenseRepository, times(0)).find(
            LocalDateTime.of(2023, 8, 23, 0, 0, 0),
            LocalDateTime.of(2023, 8, 31, 0, 0, 0),
            ApprovalState.PENDING
        )

        verify(expenseRepository, times(0)).find(
            LocalDateTime.of(2023, 8, 23, 0, 0, 0),
            LocalDateTime.of(2023, 8, 31, 0, 0, 0)
        )

        verify(expenseRepository, times(0)).find(
            ApprovalState.PENDING
        )
    }

    companion object {
        private val expenseExpected = Expense(
            1,
            1,
            LocalDateTime.of(2023, 8, 23, 0, 0, 0),
            "expense",
            BigDecimal(10.0),
            ExpenseType.MARKETING,
            ApprovalState.PENDING,
            attachments = listOf(
                AttachmentInfo(
                    UUID.randomUUID(),
                    1,
                    "/tmp",
                    "nameFile",
                    "jpeg",
                    LocalDateTime.of(2023, 8, 23, 0, 0, 0),
                    true
                )
            )
        )

        private val filterComplete = ExpenseFilterDTO(
            LocalDateTime.of(2023, 8, 23, 0, 0, 0),
            LocalDateTime.of(2023, 8, 31, 0, 0, 0),
            ApprovalState.PENDING
        )

        private val filterDateRange = ExpenseFilterDTO(
            LocalDateTime.of(2023, 8, 23, 0, 0, 0),
            LocalDateTime.of(2023, 8, 31, 0, 0, 0)
        )

        private val filterState = ExpenseFilterDTO(
            null,
            null,
            ApprovalState.PENDING
        )

        private val filterEmpty = ExpenseFilterDTO(
            null,
            null,
            null
        )
    }
}