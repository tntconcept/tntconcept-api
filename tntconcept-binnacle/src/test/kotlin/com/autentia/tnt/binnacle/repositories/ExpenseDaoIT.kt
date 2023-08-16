package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.Expense
import com.autentia.tnt.binnacle.entities.ExpenseType
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.Month
import java.util.Optional

@MicronautTest()
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ExpenseDaoIT {

    @Inject
    private lateinit var expenseDaoSut: ExpenseDao;

    @Test
    fun `should find expense by id`() {
        val expectedExpense = Optional.of(Expense(
            1,
            1,
            LocalDateTime.of(2023, Month.AUGUST, 17, 0, 0, 0),
            "Expense for tests",
            BigDecimal("10.00"),
            ExpenseType.MARKETING,
            ApprovalState.PENDING
        ))
        val actualExpenseFromData = expenseDaoSut.findById(1);
        Assertions.assertEquals(expectedExpense, actualExpenseFromData);
    }


}