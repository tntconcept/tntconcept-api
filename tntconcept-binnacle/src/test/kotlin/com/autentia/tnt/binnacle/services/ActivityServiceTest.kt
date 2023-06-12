package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.core.domain.DateInterval
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.*
import com.autentia.tnt.binnacle.entities.dto.EvidenceDTO
import com.autentia.tnt.binnacle.exception.ActivityNotFoundException
import com.autentia.tnt.binnacle.exception.NoEvidenceInActivityException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.InternalActivityRepository
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.verify
import org.mockito.BDDMockito.verifyNoInteractions
import org.mockito.BDDMockito.willDoNothing
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
    fun `get activities between start and end date with userId`() {
        val userId = 1L
        val startDate = LocalDate.of(2019, 1, 1)
        val endDate = LocalDate.of(2019, 1, 31)

        whenever(
            activityRepository.findByUserId(
                startDate.atTime(LocalTime.MIN), endDate.atTime(LocalTime.MAX), userId
            )
        ).thenReturn(listOf(activityWithoutEvidenceSaved))

        val actual = sut.getActivitiesBetweenDates(DateInterval.of(startDate, endDate), userId)

        assertEquals(listOf(activityWithoutEvidenceSaved), actual)
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
    fun `create activity without evidence`() {
        whenever(activityRepository.save(activityWithoutEvidenceToSave)).thenReturn(activityWithoutEvidenceSaved)

        val result = sut.createActivity(activityWithoutEvidence, null)

        assertEquals(activityWithoutEvidenceSaved.toDomain(), result)
        verifyNoInteractions(activityEvidenceService)
    }

    @Test
    fun `fail when create activity without evidence attached but hasEvidence is true`() {
        whenever(activityRepository.save(activityWithoutEvidenceAttachedToSave)).thenReturn(
            activityWithoutEvidenceAttachedSaved
        )

        assertThrows<NoEvidenceInActivityException> {
            sut.createActivity(activityWithoutEvidenceAttached, null)
        }

        verifyNoInteractions(activityEvidenceService)
    }

    @Test
    fun `create activity and store evidence`() {
        whenever(activityRepository.save(activityWithEvidenceToSave)).thenReturn(activityWithEvidenceSaved)

        val result = sut.createActivity(activityWithEvidence, evidence)

        assertEquals(activityWithEvidenceSaved.toDomain(), result)
        verify(activityEvidenceService).storeActivityEvidence(
            activityWithEvidenceSaved.id!!, evidence, activityWithEvidenceSaved.insertDate!!
        )
    }

    @Test
    fun `create activity with nonexistent project role`() {
        whenever(projectRoleRepository.findById(99)).thenReturn(null)

        val activityWithoutImageAndNonExistentRole =
            activityWithoutEvidence.copy(projectRole = projectRole.toDomain().copy(id = 88))

        assertThrows<IllegalStateException> {
            sut.createActivity(
                activityWithoutImageAndNonExistentRole, null
            )
        }
    }

    @Test
    fun `update activity`() {
        val activity = com.autentia.tnt.binnacle.core.domain.Activity.of(
            activityWithoutEvidenceSaved.id,
            TimeInterval.of(
                TODAY_NOON, TODAY_NOON.plusMinutes(120)
            ),
            120,
            "Description...",
            projectRole.toDomain(),
            1L,
            false,
            null,
            null,
            false,
            ApprovalState.NA,
        )

        whenever(activityRepository.findById(activityWithoutEvidenceSaved.id!!)).thenReturn(activityWithoutEvidenceSaved)

        val savedActivity = Activity.of(activity, projectRole)

        whenever(activityRepository.update(Activity.of(activity, projectRole))).thenReturn(savedActivity)

        val result = sut.updateActivity(activity, null)

        assertEquals(activity, result)
        verifyNoInteractions(activityEvidenceService)
    }

    @Test
    fun `update activity and update the stored image`() {
        val activityId = 90L
        val activityToUpdate = com.autentia.tnt.binnacle.core.domain.Activity.of(
            activityId, TimeInterval.of(
                LocalDateTime.of(LocalDate.now(), LocalTime.NOON),
                LocalDateTime.of(LocalDate.now(), LocalTime.NOON).plusMinutes(120)
            ), 120, "Description...", projectRole.toDomain(), 1L, true, null, null, true, ApprovalState.NA
        )
        val oldActivityInsertDate = Date()
        val oldActivity = Activity(
            id = activityId,
            start = activityToUpdate.getStart(),
            duration = 120,
            end = activityToUpdate.getStart().plusHours(2L),
            description = "Test activity",
            projectRole = projectRole,
            userId = USER.id,
            billable = false,
            hasEvidences = true,
            approvalState = ApprovalState.NA,
            insertDate = oldActivityInsertDate,
        )

        whenever(activityRepository.findById(activityId)).thenReturn(oldActivity)

        // Store the new image in the same old activity path
        willDoNothing().given(activityEvidenceService)
            .storeActivityEvidence(activityToUpdate.id!!, evidence, oldActivityInsertDate)

        val activityToReturn = Activity.of(activityToUpdate, projectRole)

        given(activityRepository.update(activityToReturn)).willReturn(activityToReturn)

        val result = sut.updateActivity(activityToUpdate, evidence)

        assertThat(result).isEqualTo(activityToUpdate)
        verify(activityEvidenceService).storeActivityEvidence(
            activityToUpdate.id!!, evidence, oldActivityInsertDate
        )
    }

    @Test
    fun `update activity and delete the stored image`() {
        val activityId = 90L
        val activity = com.autentia.tnt.binnacle.core.domain.Activity.of(
            activityId, TimeInterval.of(
                LocalDateTime.of(LocalDate.now(), LocalTime.NOON),
                LocalDateTime.of(LocalDate.now(), LocalTime.NOON).plusMinutes(120)
            ), 120, "Description...", projectRole.toDomain(), 1L, false, null, null, false, ApprovalState.NA
        )

        val oldActivityInsertDate = Date()
        val oldActivity = Activity(
            id = activityId,
            start = activity.getStart(),
            duration = 120,
            end = activity.getStart().plusHours(2L),
            description = "Test activity",
            projectRole = projectRole,
            userId = USER.id,
            billable = false,
            hasEvidences = true,
            approvalState = ApprovalState.NA,
            insertDate = oldActivityInsertDate,
        )

        given(activityRepository.findById(activityId)).willReturn(oldActivity)

        // Delete the old activity image
        given(activityEvidenceService.deleteActivityEvidence(activityId, oldActivityInsertDate)).willReturn(true)

        val savedActivity = Activity.of(activity, projectRole)

        given(activityRepository.update(Activity.of(activity, projectRole))).willReturn(savedActivity)

        val result = sut.updateActivity(activity, null)

        assertThat(result).isEqualTo(activity)
        verify(activityEvidenceService).deleteActivityEvidence(activityId, oldActivityInsertDate)
    }

    @Test
    fun `approve activity by id`() {
        given(activityRepository.findById(activityWithEvidenceSaved.id as Long)).willReturn(
            activityWithEvidenceSaved
        )
        given(
            activityRepository.update(
                activityWithEvidenceSaved
            )
        ).willReturn(activityWithEvidenceSaved)

        val approvedActivity = sut.approveActivityById(activityWithEvidenceSaved.id as Long)
        assertThat(approvedActivity.approvalState).isEqualTo(ApprovalState.ACCEPTED)
    }

    @Test
    fun `delete activity by id`() {
        whenever(activityRepository.findById(activityWithoutEvidenceSaved.id!!)).thenReturn(activityWithoutEvidenceSaved)

        sut.deleteActivityById(activityWithoutEvidenceSaved.id as Long)

        verify(activityRepository).deleteById(activityWithoutEvidenceSaved.id!!)
        verifyNoInteractions(activityEvidenceService)
    }

    @Test
    fun `delete activity by id and its image`() {
        whenever(activityRepository.findById(activityWithoutEvidenceSaved.id!!)).thenReturn(activityWithEvidenceSaved)

        whenever(activityRepository.findById(activityWithEvidenceSaved.id!!)).thenReturn(activityWithEvidenceSaved)

        sut.deleteActivityById(activityWithEvidenceSaved.id!!)

        verify(activityRepository).deleteById(activityWithEvidenceSaved.id!!)
        verify(activityEvidenceService).deleteActivityEvidence(
            activityWithEvidenceSaved.id!!, activityWithEvidenceSaved.insertDate!!
        )
    }

    @Test
    fun `should find overlapped activites`() {
        val userId = 1L
        val startDate = LocalDate.of(2019, 1, 1)
        val endDate = LocalDate.of(2019, 1, 31)
        val timeInterval = TimeInterval.of(
            startDate.atTime(LocalTime.MIN), endDate.atTime(LocalTime.MAX)
        )
        val expectedActivities = activities.map(Activity::toDomain)

        whenever(
            activityRepository.findOverlapped(
                startDate.atTime(LocalTime.MIN), endDate.atTime(LocalTime.MAX), userId
            )
        ).thenReturn(activities)

        val result = sut.findOverlappedActivities(startDate.atTime(LocalTime.MIN), endDate.atTime(LocalTime.MAX), userId)

        assertEquals(expectedActivities, result)
    }

    private companion object {
        private val USER = createUser()

        private val organization = Organization(1L, "Autentia", emptyList())
        private val project =
            Project(1L, "Back-end developers", true, false, LocalDate.now(), null, null, organization, emptyList())
        private val projectRole =
            ProjectRole(10, "Kotlin developer", RequireEvidence.NO, project, 0, true, false, TimeUnit.MINUTES)

        private val TODAY_NOON = LocalDateTime.of(LocalDate.now(), LocalTime.NOON)

        private const val notFoundActivityId = 1L

        private val activityWithoutEvidence = com.autentia.tnt.binnacle.core.domain.Activity.of(
            null, TimeInterval.of(
                LocalDateTime.of(LocalDate.now(), LocalTime.NOON),
                LocalDateTime.of(LocalDate.now(), LocalTime.NOON).plusMinutes(60)
            ), 60, "Dummy description", projectRole.toDomain(), 1L, false, 1L, null, false, ApprovalState.NA
        )

        private val activityWithoutEvidenceAttached = com.autentia.tnt.binnacle.core.domain.Activity.of(
            null, TimeInterval.of(
                LocalDateTime.of(LocalDate.now(), LocalTime.NOON),
                LocalDateTime.of(LocalDate.now(), LocalTime.NOON).plusMinutes(60)
            ), 60, "Dummy description", projectRole.toDomain(), 1L, false, 1L, null, true, ApprovalState.NA
        )

        private val activityWithEvidence = com.autentia.tnt.binnacle.core.domain.Activity.of(
            null, TimeInterval.of(
                LocalDateTime.of(LocalDate.now(), LocalTime.NOON),
                LocalDateTime.of(LocalDate.now(), LocalTime.NOON).plusMinutes(120)
            ), 120, "Description...", projectRole.toDomain(), 1L, false, 1L, null, true, ApprovalState.NA
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
