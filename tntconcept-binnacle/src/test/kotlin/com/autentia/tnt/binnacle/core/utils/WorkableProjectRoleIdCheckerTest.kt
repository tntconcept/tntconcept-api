package com.autentia.tnt.binnacle.core.utils

import com.autentia.tnt.binnacle.core.domain.ProjectRoleId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class WorkableProjectRoleIdCheckerTest {

    private val sut = WorkableProjectRoleIdChecker(listOf(NOT_WORKABLE_PROJECT_ROLE_ID_1))

    @ParameterizedTest
    @MethodSource("projectRoleParametersProvider")
    fun `should return workable`(
        testDescription: String,
        projectRoleId: ProjectRoleId,
        expectedIsWorkable: Boolean
    ) {
        val workable = sut.isWorkable(projectRoleId)

        assertEquals(expectedIsWorkable, workable)
    }

    private companion object {
        private val NOT_WORKABLE_PROJECT_ROLE_ID_1 = ProjectRoleId(1L)

        @JvmStatic
        private fun projectRoleParametersProvider() = listOf(
            Arguments.of(
                "given workable project role id should return true",
                ProjectRoleId(5L),
                true
            ),
            Arguments.of(
                "given not workable project role id should return false",
                NOT_WORKABLE_PROJECT_ROLE_ID_1,
                false
            )
        )
    }
}
