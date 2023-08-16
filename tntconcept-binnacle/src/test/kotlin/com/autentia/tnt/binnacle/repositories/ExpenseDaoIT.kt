package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.AttachmentInfo
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
import java.util.*

@MicronautTest()
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ExpenseDaoIT {

    @Inject
    private lateinit var expenseDaoSut: ExpenseDao;

    @Test
    fun `should find expense without attachment by id`() {
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

    @Test
    fun `should find expense with attachment by id`() {
        val expectedExpense = Optional.of(
            Expense(
                2,
                1,
                LocalDateTime.of(2023, Month.AUGUST, 17, 0, 0, 0),
                "Expense for tests",
                BigDecimal("10.00"),
                ExpenseType.MARKETING,
                ApprovalState.PENDING,
                attachmens = listOf(
                    AttachmentInfo(
                        id = UUID.fromString("4d3cbe3f-369f-11ee-99c2-0242ac180003"),
                        userId = 11,
                        path = "path/to/test/file",
                        fileName = "testFile,jpg",
                        mimeType = "image/jpeg",
                        uploadDate = LocalDateTime.of(2023, 8, 17, 0, 0, 0),
                        isTemporary = true
                    )
                )
            )
        )
        val actualExpenseFromData = expenseDaoSut.findById(2);
        Assertions.assertEquals(expectedExpense, actualExpenseFromData);
    }

    @Test
    fun `should find all expense by date range`() {

    }

    @Test
    fun `should find all expense by status`() {

    }

    @Test
    fun `should find all expense by user`() {

    }


    @Test
    fun `should find all expense by date range and status`() {

    }

    @Test
    fun `should find all expense by date range and status and user`() {

    }

    @Test
    fun `should save a new expense without attachment`() {

    }

    @Test
    fun `should save a new expense with attachment`() {

    }

    @Test
    fun `should update a exist expense without attachment`() {

    }

    @Test
    fun `should update a exist expense with attachment`() {

    }

    @Test
    fun `should delete an exist expense without attachment`() {

    }

    @Test
    fun `should delete an exist expense with attachment`() {

    }

}