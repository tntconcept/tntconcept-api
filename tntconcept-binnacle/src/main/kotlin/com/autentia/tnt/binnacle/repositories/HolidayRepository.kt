package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.Holiday
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository
import java.time.LocalDateTime

@Repository
internal interface HolidayRepository : CrudRepository<Holiday, Long> {

    fun findAllByDateBetween(startDate: LocalDateTime, endDate: LocalDateTime): List<Holiday>

}
