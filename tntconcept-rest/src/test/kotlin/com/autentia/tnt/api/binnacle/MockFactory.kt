package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.core.domain.ProjectRoleId
import com.autentia.tnt.binnacle.core.utils.WorkableProjectRoleIdChecker
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import jakarta.inject.Singleton

@Factory
internal class MockFactory {

    @Singleton
    @Replaces
    fun workableProjectRoleIdChecker() = WorkableProjectRoleIdChecker(listOf(ProjectRoleId(2)))

}
