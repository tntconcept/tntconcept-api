package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.Expense
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaSpecificationExecutor
import io.micronaut.data.repository.CrudRepository
@Repository
internal interface ExpenseDao:CrudRepository<Expense,Long>,JpaSpecificationExecutor<Expense> {

}
