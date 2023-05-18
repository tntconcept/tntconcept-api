package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.*
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*


@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActivityMissingEvidenceSpecificationIT {

    @Inject
    private lateinit var projectRoleDao: ProjectRoleDao

    @Inject
    private lateinit var organizationRepository: OrganizationRepository

    @Test
    fun `ey`() {
        assertThat(organizationRepository.findById(3L)).isPresent()
    }
    
}

//        val projectA = createProject().copy(id = 101L, name = "Project A", organization = organizationA)
//        val projectB = createProject().copy(id = 102L, name = "Project B", organization = organizationA)
//        val projectC = createProject().copy(id = 103L, name = "Project B", organization = organizationB)
//        projectRepository.saveAll(listOf(projectA, projectB, projectC))
//
//        val projectARoleWeekly = createProjectRole().copy(
//            id = 101L,
//            name = "projectARoleWeekly",
//            requireEvidence = RequireEvidence.WEEKLY,
//            project = projectA,
//            isApprovalRequired = true,
//            maxAllowed = 2,
//            timeUnit = TimeUnit.DAYS,
//            isWorkingTime = true
//        )
//
//        val projectARoleWeekly2 = createProjectRole().copy(
//            id = 102L,
//            name = "projectARoleWeekly TWO",
//            requireEvidence = RequireEvidence.WEEKLY,
//            project = projectA,
//            isApprovalRequired = true,
//            maxAllowed = 2,
//            timeUnit = TimeUnit.DAYS,
//            isWorkingTime = true
//        )
//
//        val projectARoleOnce = createProjectRole().copy(
//            id = 103L,
//            name = "projectARoleOnce",
//            requireEvidence = RequireEvidence.ONCE,
//            project = projectA,
//            isApprovalRequired = true,
//            maxAllowed = 10,
//            timeUnit = TimeUnit.DAYS,
//            isWorkingTime = true
//        )
//
//        val projectBRoleWeekly = createProjectRole().copy(
//            id = 104L,
//            name = "projectBRoleWeekly",
//            requireEvidence = RequireEvidence.WEEKLY,
//            project = projectB,
//            isApprovalRequired = true,
//            maxAllowed = 2,
//            timeUnit = TimeUnit.DAYS,
//            isWorkingTime = true
//        )
//        val projectCRoleWeeklyOrganizationB = createProjectRole().copy(
//            id = 105L,
//            name = "projectCRoleWeeklyOrganizationB",
//            requireEvidence = RequireEvidence.WEEKLY,
//            project = projectC,
//            isApprovalRequired = true,
//            maxAllowed = 2,
//            timeUnit = TimeUnit.DAYS,
//            isWorkingTime = true
//        )
//        projectRoleDao.saveAll(
//            listOf(
//                projectARoleWeekly,
//                projectARoleWeekly2,
//                projectARoleOnce,
//                projectBRoleWeekly,
//                projectCRoleWeeklyOrganizationB
//            )
//        )
//
//        val userFirst = createUser().copy(id = 102, email = "userFirst@example.com")
//        val userSecond = createUser().copy(id = 103, email = "userSecond@example.com")
//        val userThird = createUser().copy(id = 104, email = "userThird@example.com")
//        userDao.saveAll(listOf(userFirst, userSecond, userThird))
//}

//    @Test
//    fun `should find activities without evidence and required evidence once`() {
//        val projectRole = projectRoleDao.findById(1L).get()
//        val projectRoleEdited = projectRole.copy(requireEvidence = RequireEvidence.ONCE)
//        projectRoleDao.update(projectRoleEdited)
//        val projectRoleWithoutEvidenceOnce = projectRoleDao.findById(3).get()
//        val activityWithoutEvidence = Activity(
//            start = yesterday.atTime(8, 0, 0),
//            end = yesterday.atTime(17, 0, 0),
//            duration = 540,
//            description = "Test activity 2",
//            projectRole = projectRole,
//            userId = userId,
//            billable = false,
//            hasEvidences = false,
//            approvalState = ApprovalState.PENDING
//        )
//        val activityWithEvidence = Activity(
//            start = yesterday.atTime(8, 0, 0),
//            end = yesterday.atTime(17, 0, 0),
//            duration = 540,
//            description = "Test activity 3",
//            projectRole = projectRole,
//            userId = userId,
//            billable = false,
//            hasEvidences = true,
//            approvalState = ApprovalState.PENDING
//        )
//        val activityWithoutEvidenceNeeded = Activity(
//            start = yesterday.atTime(8, 0, 0),
//            end = yesterday.atTime(17, 0, 0),
//            duration = 540,
//            description = "Test activity 4",
//            projectRole = projectRoleWithoutEvidenceOnce,
//            userId = userId,
//            billable = false,
//            hasEvidences = false,
//            approvalState = ApprovalState.PENDING
//        )
//        val activityWithAutogeneratedEvidence = Activity(
//            start = yesterday.atTime(8, 0, 0),
//            end = yesterday.atTime(17, 0, 0),
//            duration = 540,
//            description = "###Autocreated evidence###\n(DO NOT DELETE)\n ETC. ETC.",
//            projectRole = projectRole,
//            userId = userId,
//            billable = false,
//            hasEvidences = false,
//            approvalState = ApprovalState.PENDING
//        )
//
//        activityDao.saveAll(
//            listOf(
//                activityWithoutEvidence,
//                activityWithEvidence,
//                activityWithoutEvidenceNeeded,
//                activityWithAutogeneratedEvidence
//            )
//        )
//
//        val predicate = ActivityPredicates.missingEvidenceOnce()
//        val result = activityDao.findAll(predicate)
//
//        val expectedResults = listOf(activityWithoutEvidence)
//        assertEquals(expectedResults, result)
//    }
//
//    @Test
//    fun `should find activities missing evidence with required evidence weekly`() {
//        var projectRole: ProjectRole = projectRoleDao.findById(1).orElseThrow { fail() }
//        projectRole = projectRole.copy(requireEvidence = RequireEvidence.WEEKLY)
//        projectRoleDao.update(projectRole)
//        val otherProjectRole: ProjectRole = projectRoleDao.findById(2).orElseThrow { fail() }
//        val activityWithEvidence = Activity(
//            start = today.atTime(8, 0, 0).minusDays(3),
//            end = today.atTime(17, 0, 0).minusDays(3),
//            duration = 540,
//            description = "###Autocreated evidence###\n(DO NOT DELETE)\n ETC. ETC.",
//            projectRole = projectRole,
//            userId = userId,
//            billable = false,
//            hasEvidences = false,
//            approvalState = ApprovalState.PENDING
//        )
//        val activityHasPreviousEvidence = Activity(
//            start = today.atTime(8, 0, 0),
//            end = today.atTime(17, 0, 0),
//            duration = 540,
//            description = "This should NOT be returned",
//            projectRole = projectRole,
//            userId = userId,
//            billable = false,
//            hasEvidences = false,
//            approvalState = ApprovalState.PENDING
//        )
//        val activityEvidenceTooOld = Activity(
//            start = today.atTime(8, 0, 0).minusDays(8),
//            end = today.atTime(17, 0, 0).minusDays(8),
//            duration = 540,
//            description = "###Autocreated evidence###\n(DO NOT DELETE)\n ETC. ETC.",
//            projectRole = otherProjectRole,
//            userId = userId,
//            billable = false,
//            hasEvidences = false,
//            approvalState = ApprovalState.PENDING
//        )
//        val activityWithoutEvidence = Activity(
//            start = today.atTime(8, 0, 0),
//            end = today.atTime(17, 0, 0),
//            duration = 540,
//            description = "This should be returned",
//            projectRole = otherProjectRole,
//            userId = userId,
//            billable = false,
//            hasEvidences = false,
//            approvalState = ApprovalState.PENDING
//        )
//        activityDao.saveAll(
//            listOf(
//                activityWithEvidence,
//                activityEvidenceTooOld,
//                activityHasPreviousEvidence,
//                activityWithoutEvidence,
//            )
//        )
//
//        val predicate = ActivityPredicates.missingEvidenceWeekly()
//        val results = activityDao.findAll(predicate)
//
//        val expectedResults: List<Activity> = listOf(activityWithoutEvidence)
//        assertEquals(expectedResults.size, results.size)
//    }
