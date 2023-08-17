package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.Expense
import io.micronaut.data.annotation.Query
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository
import java.time.LocalDateTime
import java.util.*

@Repository
internal interface ExpenseDao:CrudRepository<Expense,Long> {
    @Query("SELECT a FROM Expense a WHERE a.date >= :startDate AND a.date <= :endDate")
    fun find(startDate: LocalDateTime, endDate: LocalDateTime): List<Expense>
    @Query("SELECT a FROM Expense a WHERE a.state = :status")
    fun find(status:ApprovalState): List<Expense>
    @Query("SELECT a FROM Expense a WHERE a.userId = :userId")
    fun find(userId:Long): List<Expense>
    @Query("SELECT a FROM Expense a WHERE a.date >= :startDate AND a.date <= :endDate AND a.userId= :userId")
    fun find(startDate: LocalDateTime, endDate: LocalDateTime,userId:Long): List<Expense>
    @Query("SELECT a FROM Expense a WHERE a.date >= :startDate AND a.date <= :endDate AND a.userId= :userId AND a.state = :status")
    fun find(startDate: LocalDateTime, endDate: LocalDateTime,userId:Long,status: ApprovalState): List<Expense>
    @Query("SELECT a FROM Expense a WHERE a.id=:id AND a.userId = :userId")
    fun find(id: Long, userId: Long): Optional<Expense>
    @Query("SELECT a FROM Expense a WHERE a.state = :status AND a.userId=:userId")
    fun find(status:ApprovalState,userId:Long): List<Expense>
    @Query("SELECT a FROM Expense a WHERE a.date >= :startDate AND a.date <= :endDate AND a.state=:status")
    fun find(startDate: LocalDateTime, endDate: LocalDateTime,status: ApprovalState): List<Expense>

}
