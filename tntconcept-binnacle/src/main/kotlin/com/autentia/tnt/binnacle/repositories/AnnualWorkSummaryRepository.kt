package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.AnnualWorkSummary
import com.autentia.tnt.binnacle.entities.AnnualWorkSummaryId
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository

@Repository
internal interface AnnualWorkSummaryRepository : CrudRepository<AnnualWorkSummary, AnnualWorkSummaryId>
