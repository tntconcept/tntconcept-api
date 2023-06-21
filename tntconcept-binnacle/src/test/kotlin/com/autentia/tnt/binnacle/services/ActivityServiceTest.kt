package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.core.domain.DateInterval
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.entities.dto.EvidenceDTO
import com.autentia.tnt.binnacle.exception.ActivityNotFoundException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.InternalActivityRepository
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.mockito.kotlin.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

@TestInstance(Lifecycle.PER_CLASS)
internal class ActivityServiceTest {
    private val activityRepository = mock<ActivityRepository>()
    private val internalActivityRepository = mock<InternalActivityRepository>()
    private val projectRoleRepository = mock<ProjectRoleRepository>()
    private val activityEvidenceService = mock<ActivityEvidenceService>()

    private val sut = ActivityService(
        activityRepository, internalActivityRepository, projectRoleRepository, activityEvidenceService
    )

    @AfterEach
    fun resetMocks() {
        reset(activityRepository, internalActivityRepository, projectRoleRepository, activityEvidenceService)
    }

    @BeforeEach
    fun setMocks() {
        whenever(projectRoleRepository.findById(projectRole.id)).thenReturn(projectRole)
    }

    @Test
    fun `get activity by id`() {
        whenever(activityRepository.findById(activityWithoutEvidenceSaved.id!!)).thenReturn(
            activityWithoutEvidenceSaved
        )

        val actual = sut.getActivityById(activityWithoutEvidenceSaved.id!!)

        assertEquals(activityWithoutEvidenceSaved.toDomain(), actual)
    }

    @Test
    fun `fail when the activity was not found by id`() {
        assertThrows<ActivityNotFoundException> {
            sut.getActivityById(notFoundActivityId)
        }
    }

    @Test
    fun `get activities between start and end date for user`() {
        val startDate = LocalDate.of(2019, 1, 1)
        val endDate = LocalDate.of(2019, 1, 31)
        val userId = 1L

        whenever(
            internalActivityRepository.findByUserId(
                startDate.atTime(LocalTime.MIN), endDate.atTime(LocalTime.MAX), userId
            )
        ).thenReturn(listOf(activityWithoutEvidenceSaved))

        val actual = sut.getUserActivitiesBetweenDates(DateInterval.of(startDate, endDate), userId)

        assertEquals(listOf(activityWithoutEvidenceSaved), actual)
    }

    @Test
    fun `get activities by project role id and user id`() {
        val expectedProjectRoleActivities = listOf(activityWithoutEvidenceSaved, activityWithEvidenceToSave)
        val userId = 1L

        whenever(activityRepository.findByProjectRoleIdAndUserId(1L, userId)).thenReturn(expectedProjectRoleActivities)

        val result = sut.getProjectRoleActivities(1L, userId)

        assertEquals(expectedProjectRoleActivities, result)
    }

    @Test
    fun `get activities by time interval should call repository`() {
        val userIds = listOf(1L)

        doReturn(activities).whenever(activityRepository).find(timeInterval.start, timeInterval.end, userIds)

        assertEquals(activities, sut.getActivities(timeInterval, userIds))
    }

    @Test
    fun `get activities by project should call repository`() {
        val userId = 1L
        whenever(activityRepository.findByProjectId(timeInterval.start, timeInterval.end, 1L, userId)).thenReturn(
            activities
        )
        assertEquals(activities, sut.getActivitiesByProjectId(timeInterval, 1L, userId))
    }

    @Test
    fun testGetActivitiesOfLatestProjects() {
        val userId = 1L
        whenever(activityRepository.findOfLatestProjects(timeInterval.start, timeInterval.end, userId)).thenReturn(
            activities
        )
        assertEquals(activities, sut.getActivitiesOfLatestProjects(timeInterval, userId))
    }

    @Test
    fun `get activities by project role ids with user id`() {
        val userId = 1L
        val startDate = LocalDate.of(2019, 1, 1)
        val endDate = LocalDate.of(2019, 1, 31)
        val projectRoles = listOf(1L)
        val timeInterval = TimeInterval.of(
            startDate.atTime(LocalTime.MIN), endDate.atTime(LocalTime.MAX)
        )
        val expectedActivities = activities.map(Activity::toDomain)

        whenever(
            activityRepository.findByProjectRoleIds(
                startDate.atTime(LocalTime.MIN), endDate.atTime(LocalTime.MAX), projectRoles, userId
            )
        ).thenReturn(activities)

        val result = sut.getActivitiesByProjectRoleIds(timeInterval, projectRoles, userId)

        assertEquals(expectedActivities, result)
    }


    @Test
    fun `should find overlapped activities`() {
        val userId = 1L
        val startDate = LocalDate.of(2019, 1, 1)
        val endDate = LocalDate.of(2019, 1, 31)
        val expectedActivities = activities.map(Activity::toDomain)

        whenever(
            activityRepository.findOverlapped(
                startDate.atTime(LocalTime.MIN), endDate.atTime(LocalTime.MAX), userId
            )
        ).thenReturn(activities)

        val result =
            sut.findOverlappedActivities(startDate.atTime(LocalTime.MIN), endDate.atTime(LocalTime.MAX), userId)

        assertEquals(expectedActivities, result)
    }

    private companion object {

        private val organization = Organization(1L, "Autentia", emptyList())
        private val project =
            Project(1L, "Back-end developers", true, false, LocalDate.now(), null, null, organization, emptyList())
        private val projectRole =
            ProjectRole(10, "Kotlin developer", RequireEvidence.NO, project, 0, true, false, TimeUnit.MINUTES)

        private const val notFoundActivityId = 1L

        private val activityWithoutEvidence = com.autentia.tnt.binnacle.core.domain.Activity.of(
            null, TimeInterval.of(
            LocalDateTime.of(LocalDate.now(), LocalTime.NOON),
            LocalDateTime.of(LocalDate.now(), LocalTime.NOON).plusMinutes(60)
        ), 60, "Dummy description", projectRole.toDomain(), 1L, false, 1L, null, false, ApprovalState.NA, null
        )

        private val activityWithoutEvidenceAttached = com.autentia.tnt.binnacle.core.domain.Activity.of(
            null, TimeInterval.of(
            LocalDateTime.of(LocalDate.now(), LocalTime.NOON),
            LocalDateTime.of(LocalDate.now(), LocalTime.NOON).plusMinutes(60)
        ), 60, "Dummy description", projectRole.toDomain(), 1L, false, 1L, null, true, ApprovalState.NA, null
        )

        private val activityWithEvidence = com.autentia.tnt.binnacle.core.domain.Activity.of(
            null, TimeInterval.of(
            LocalDateTime.of(LocalDate.now(), LocalTime.NOON),
            LocalDateTime.of(LocalDate.now(), LocalTime.NOON).plusMinutes(120)
        ), 120, "Description...", projectRole.toDomain(), 1L, false, 1L, null, true, ApprovalState.NA, null
        )

        private val evidence = EvidenceDTO.from("data:application/pdf;base64,SGVsbG8gV29ybGQh")

        private val activityWithEvidenceToSave = Activity.of(activityWithEvidence, projectRole)
        private val activityWithoutEvidenceToSave = Activity.of(activityWithoutEvidence, projectRole)
        private val activityWithoutEvidenceAttachedToSave = Activity.of(activityWithoutEvidenceAttached, projectRole)

        private val activityWithEvidenceSaved =
            activityWithEvidenceToSave.copy(id = 101, insertDate = Date(), approvalState = ApprovalState.PENDING)

        private val activityWithoutEvidenceSaved =
            activityWithoutEvidenceToSave.copy(id = 100L, insertDate = Date(), approvalState = ApprovalState.PENDING)

        private val activityWithoutEvidenceAttachedSaved =
            activityWithoutEvidenceToSave.copy(id = 100L, insertDate = Date(), approvalState = ApprovalState.PENDING)

        private val activities = listOf(activityWithoutEvidenceSaved)

        private val timeInterval = TimeInterval.of(LocalDateTime.now(), LocalDateTime.now().plusMinutes(30))
    }

}
