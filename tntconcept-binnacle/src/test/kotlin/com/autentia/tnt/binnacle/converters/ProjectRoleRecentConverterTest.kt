package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.core.domain.ProjectRoleRecent
import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.dto.ProjectRoleRecentDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.Month.FEBRUARY
import java.time.Month.JANUARY

internal class ProjectRoleRecentConverterTest {

    private var projectRoleRecent = ProjectRoleRecentConverter()

    @Test
    fun `given domain ProjectRoleRecent list should return ProjectRoleRecentDTO list with converted values`() {

        val projectRoleRecentList = PROJECT_ROLES_RECENT

        val projectRoleRecentDTOList = projectRoleRecentList.map { projectRoleRecent.toProjectRoleRecentDTO(it) }

        assertEquals(PROJECT_ROLES_RECENT_DTO, projectRoleRecentDTOList)
    }

    private companion object{

        private val PROJECT_ROLES_RECENT = listOf(
            ProjectRoleRecent(
                id = 1L,
                name = "First Project Role",
                requireEvidence = RequireEvidence.NO,
                date = LocalDateTime.of(2020, JANUARY, 1, 0, 0, 0),
                organizationName = "Dummy Organization",
                projectName = "Dummy Project",
                projectBillable = false,
                projectOpen = true
            ),
            ProjectRoleRecent(
                id = 2L,
                name = "Second Project Role",
                requireEvidence = RequireEvidence.NO,
                date = LocalDateTime.of(2021, FEBRUARY, 1, 0, 0, 0),
                organizationName = "Dummy Organization",
                projectName = "Dummy Project",
                projectBillable = true,
                projectOpen = true
            ),
        )

        private val PROJECT_ROLES_RECENT_DTO = listOf(
            ProjectRoleRecentDTO(
                id = 1L,
                name = "First Project Role",
                requireEvidence = RequireEvidence.NO,
                date = LocalDateTime.of(2020, JANUARY, 1, 0, 0, 0),
                organizationName = "Dummy Organization",
                projectName = "Dummy Project",
                projectBillable = false,
                projectOpen = true
            ),
            ProjectRoleRecentDTO(
                id = 2L,
                name = "Second Project Role",
                requireEvidence = RequireEvidence.NO,
                date = LocalDateTime.of(2021, FEBRUARY, 1, 0, 0, 0),
                organizationName = "Dummy Organization",
                projectName = "Dummy Project",
                projectBillable = true,
                projectOpen = true
            ),
        )

    }

}
