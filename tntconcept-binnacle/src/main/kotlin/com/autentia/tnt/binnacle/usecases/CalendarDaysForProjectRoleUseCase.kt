package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.core.domain.CalendarFactory
import com.autentia.tnt.binnacle.core.domain.DateInterval
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.exception.ProjectRoleNotFoundException
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import jakarta.inject.Singleton
import java.time.LocalDate

@Singleton
class CalendarDaysForProjectRoleUseCase internal constructor(
    private val calendarFactory: CalendarFactory,
    private val projectRoleRepository: ProjectRoleRepository,
) {
    fun get(startDate: LocalDate, endDate: LocalDate, roleId: Long): Int {
        val projectRoleEntity = projectRoleRepository.findById(roleId) ?: throw ProjectRoleNotFoundException(
            roleId
        )
        val calendar = calendarFactory.create(DateInterval.of(startDate, endDate))

        return when(projectRoleEntity.timeUnit){
            TimeUnit.DAYS -> calendar.workableDays.size
            TimeUnit.NATURAL_DAYS -> calendar.allDays.size
            TimeUnit.MINUTES -> 0
        }
    }


}