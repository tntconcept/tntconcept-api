package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.AnnualWorkSummary
import com.autentia.tnt.binnacle.entities.AnnualWorkSummaryId
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository
import javax.transaction.Transactional

@Repository
abstract class AnnualWorkSummaryRepository : CrudRepository<AnnualWorkSummary, AnnualWorkSummaryId> {
    /**
     * Because AnnualWorkSummary Entity is using a compound Natural Primary Key,
     * the JPA implementation is not being able to know if the entity that is going to be persisted is new or not, because the ID is never null.
     * This method has been created to make explicit to the service this problem but delegating to the repository the implementation.
     */
    fun saveOrUpdate(annualWorkSummary: AnnualWorkSummary): AnnualWorkSummary =
        if (existsById(annualWorkSummary.annualWorkSummaryId)) {
            update(annualWorkSummary)
        } else {
            save(annualWorkSummary)
        }
}

