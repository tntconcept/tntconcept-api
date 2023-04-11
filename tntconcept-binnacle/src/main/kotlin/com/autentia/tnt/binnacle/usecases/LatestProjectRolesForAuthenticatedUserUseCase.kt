package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.core.domain.ProjectRoleRecent
import com.autentia.tnt.binnacle.core.domain.StartEndLocalDateTime
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import java.time.LocalDate
import java.time.LocalTime
import javax.transaction.Transactional

@Singleton
class LatestProjectRolesForAuthenticatedUserUseCase internal constructor(
    private val projectRoleRepository: ProjectRoleRepository
) {
    @Transactional
    @ReadOnly
    fun get(): List<ProjectRoleRecent> {
        val oneMonthDateRange = oneMonthDateRangeFromCurrentDate()

        val roles = projectRoleRepository.findDistinctRolesBetweenDate(
            oneMonthDateRange.startDate,
            oneMonthDateRange.endDate
        )

        return roles
            .filter { it.projectOpen }
            .sortedByDescending { it.date }
            .distinctBy { it.id }
    }

    private fun oneMonthDateRangeFromCurrentDate(): StartEndLocalDateTime {
        val now = LocalDate.now()
        val startDate = now.minusMonths(1).atTime(LocalTime.MIN)
        val endDate = now.atTime(23, 59, 59)

        return StartEndLocalDateTime(startDate, endDate)
    }

}
