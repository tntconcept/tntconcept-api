package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.converters.ActivityRequestBodyConverter
import com.autentia.tnt.binnacle.core.domain.DateInterval
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.exception.ActivityNotFoundException
import com.autentia.tnt.binnacle.exception.InvalidActivityApprovalStateException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.verify
import org.mockito.BDDMockito.verifyNoInteractions
import org.mockito.BDDMockito.willDoNothing
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Date

internal class ActivityServiceTest {

    private val activityRepository = mock<ActivityRepository>()
    private val projectRoleRepository = mock<ProjectRoleRepository>()
    private val activityImageService = mock<ActivityImageService>()
    private val activityRequestBodyConverter = ActivityRequestBodyConverter()
    private val activityService = ActivityService(
        activityRepository,
        projectRoleRepository,
        activityImageService
    )

    init {
        whenever(projectRoleRepository.findById(projectRole.id)).thenReturn(projectRole)
    }

    private val activityWithImageToSave = Activity.of(activityWithImage, projectRole)
    private val activityWithoutImageToSave = Activity.of(activityWithoutImage, projectRole)

    private val activityWithImageSaved =
        activityWithImageToSave.copy(id = 101, insertDate = Date(), approvalState = ApprovalState.PENDING)

    private val activityWithoutImageSaved =
        activityWithoutImageToSave.copy(id = 100L, insertDate = Date(), approvalState = ApprovalState.PENDING)

    private val activities = listOf(activityWithoutImageSaved)

    private val timeInterval = TimeInterval.of(LocalDateTime.now(), LocalDateTime.now().plusMinutes(30))

    @Test
    fun `get activity by id`() {
        whenever(activityRepository.findById(activityWithoutImageSaved.id!!)).thenReturn(
            activityWithoutImageSaved
        )

        val actual = activityService.getActivityById(activityWithoutImageSaved.id!!)

        assertEquals(activityWithoutImageSaved.toDomain(), actual)
    }

    @Test
    fun `fail when the activity was not found by id`() {
        assertThrows<ActivityNotFoundException> {
            activityService.getActivityById(notFoundActivityId)
        }
    }

    @Test
    fun `get activities between start and end date`() {
        val startDate = LocalDate.of(2019, 1, 1)
        val endDate = LocalDate.of(2019, 1, 31)

        whenever(
            activityRepository.find(
                startDate.atTime(LocalTime.MIN),
                endDate.atTime(LocalTime.MAX)
            )
        ).thenReturn(listOf(activityWithoutImageSaved))

        val actual = activityService.getActivitiesBetweenDates(DateInterval.of(startDate, endDate))

        assertEquals(listOf(activityWithoutImageSaved), actual)
    }

    @Test
    fun `get activities between start and end date for user`() {
        val startDate = LocalDate.of(2019, 1, 1)
        val endDate = LocalDate.of(2019, 1, 31)
        val userId = 1L

        whenever(
            activityRepository.findWithoutSecurity(
                startDate.atTime(LocalTime.MIN),
                endDate.atTime(LocalTime.MAX),
                userId
            )
        ).thenReturn(listOf(activityWithoutImageSaved))

        val actual = activityService.getUserActivitiesBetweenDates(DateInterval.of(startDate, endDate), userId)

        assertEquals(listOf(activityWithoutImageSaved), actual)
    }

    @Test
    fun `get activities by project role id`() {
        val expectedProjectRoleActivities = listOf(activityWithoutImageSaved, activityWithImageToSave)

        whenever(activityRepository.find(1L)).thenReturn(expectedProjectRoleActivities)

        val result = activityService.getProjectRoleActivities(1L)

        assertEquals(expectedProjectRoleActivities, result)
    }

    @Test
    fun `get activities by approval state should call repository`() {
        doReturn(activities).whenever(activityRepository).find(ApprovalState.ACCEPTED)

        assertEquals(activities, activityService.getActivitiesApprovalState(ApprovalState.ACCEPTED))
    }

    @Test
    fun `get activities by time interval should call repository`() {
        val userIds = listOf(1L)

        doReturn(activities).whenever(activityRepository).find(timeInterval.start, timeInterval.end, userIds)

        assertEquals(activities, activityService.getActivities(timeInterval, userIds))
    }

    @Test
    fun `get activities by project should call repository`() {

        doReturn(activities).whenever(activityRepository).findByProjectId(timeInterval.start, timeInterval.end, 1L)

        assertEquals(activities, activityService.getActivitiesByProjectId(timeInterval, 1L))
    }

    @Test
    fun testGetActivitiesOfLatestProjects() {

        doReturn(activities).whenever(activityRepository).findOfLatestProjects(timeInterval.start, timeInterval.end)

        assertEquals(activities, activityService.getActivitiesOfLatestProjects(timeInterval))
    }

    @Test
    fun `create activity`() {
        whenever(activityRepository.save(activityWithoutImageToSave)).thenReturn(activityWithoutImageSaved)

        val result = activityService.createActivity(activityWithoutImage, null)

        assertEquals(activityWithoutImageSaved.toDomain(), result)
        verifyNoInteractions(activityImageService)
    }

    @Test
    fun `create activity and store image file`() {
        whenever(activityRepository.save(activityWithImageToSave)).thenReturn(activityWithImageSaved)

        val result = activityService.createActivity(activityWithImage, image)

        assertEquals(activityWithImageSaved.toDomain(), result)
        verify(activityImageService).storeActivityImage(
            activityWithImageSaved.id!!,
            image,
            activityWithImageSaved.insertDate!!
        )
    }

    @Test
    fun `create activity with nonexistent project role`() {
        whenever(projectRoleRepository.findById(99)).thenReturn(null)

        val activityWithoutImageAndNonExistentRole =
            activityWithoutImage.copy(projectRole = projectRole.toDomain().copy(id = 88))

        assertThrows<IllegalStateException> {
            activityService.createActivity(
                activityWithoutImageAndNonExistentRole,
                null
            )
        }
    }

    @Test
    fun `update activity`() {
        val activity = com.autentia.tnt.binnacle.core.domain.Activity.of(
            activityWithoutImageSaved.id,
            TimeInterval.of(
                TODAY_NOON,
                TODAY_NOON.plusMinutes(120)
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

        whenever(activityRepository.findById(activityWithoutImageSaved.id!!)).thenReturn(activityWithoutImageSaved)

        val savedActivity = Activity.of(activity, projectRole)

        whenever(activityRepository.update(Activity.of(activity, projectRole))).thenReturn(savedActivity)

        val result = activityService.updateActivity(activity, null)

        assertEquals(activity, result)
        verifyNoInteractions(activityImageService)
    }

    @Test
    fun `update activity and update the stored image`() {
        val activityId = 90L
        val activityToUpdate = com.autentia.tnt.binnacle.core.domain.Activity.of(
            activityId,
            TimeInterval.of(
                LocalDateTime.of(LocalDate.now(), LocalTime.NOON),
                LocalDateTime.of(LocalDate.now(), LocalTime.NOON).plusMinutes(120)
            ),
            120,
            "Description...",
            projectRole.toDomain(),
            1L,
            true,
            null,
            null,
            true,
            ApprovalState.NA
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
        willDoNothing().given(activityImageService)
            .storeActivityImage(activityToUpdate.id!!, image, oldActivityInsertDate)

        val activityToReturn = Activity.of(activityToUpdate, projectRole)

        given(activityRepository.update(Activity.of(activityToUpdate, projectRole))).willReturn(activityToReturn)

        val result = activityService.updateActivity(activityToUpdate, image)

        assertThat(result).isEqualTo(activityToUpdate)
        verify(activityImageService).storeActivityImage(
            activityToUpdate.id!!,
            image,
            oldActivityInsertDate
        )
    }

    @Test
    fun `update activity and delete the stored image`() {
        val activityId = 90L
        val activity = com.autentia.tnt.binnacle.core.domain.Activity.of(
            activityId,
            TimeInterval.of(
                LocalDateTime.of(LocalDate.now(), LocalTime.NOON),
                LocalDateTime.of(LocalDate.now(), LocalTime.NOON).plusMinutes(120)
            ),
            120,
            "Description...",
            projectRole.toDomain(),
            1L,
            false,
            null,
            null,
            false,
            ApprovalState.NA
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
        given(activityImageService.deleteActivityImage(activityId, oldActivityInsertDate)).willReturn(true)

        val savedActivity = Activity.of(activity, projectRole)

        given(activityRepository.update(Activity.of(activity, projectRole))).willReturn(savedActivity)

        val result = activityService.updateActivity(activity, "")

        assertThat(result).isEqualTo(activity)
        verify(activityImageService).deleteActivityImage(activityId, oldActivityInsertDate)
    }

    @Test
    fun `approve activity by id`() {
        given(activityRepository.findById(activityWithoutImageSaved.id as Long)).willReturn(activityWithoutImageSaved)
        given(
            activityRepository.update(
                activityWithoutImageSaved
            )
        ).willReturn(activityWithoutImageSaved)

        val approvedActivity = activityService.approveActivityById(activityWithoutImageSaved.id as Long)
        assertThat(approvedActivity.approvalState).isEqualTo(ApprovalState.ACCEPTED)
    }

    @Test
    fun `approve activity with not allowed state`() {
        doReturn(activityWithoutImageToSave.copy(approvalState = ApprovalState.ACCEPTED)).whenever(activityRepository)
            .findById(any())
        assertThrows<InvalidActivityApprovalStateException> {
            activityService.approveActivityById(any())
        }
    }

    @Test
    fun `delete activity by id`() {
        whenever(activityRepository.findById(activityWithoutImageSaved.id!!)).thenReturn(activityWithoutImageSaved)

        activityService.deleteActivityById(activityWithoutImageSaved.id as Long)

        verify(activityRepository).deleteById(activityWithoutImageSaved.id!!)
        verifyNoInteractions(activityImageService)
    }

    @Test
    fun `delete activity by id and its image`() {
        whenever(activityRepository.findById(activityWithoutImageSaved.id!!)).thenReturn(activityWithImageSaved)

        whenever(activityRepository.findById(activityWithImageSaved.id!!)).thenReturn(activityWithImageSaved)

        activityService.deleteActivityById(activityWithImageSaved.id!!)

        verify(activityRepository).deleteById(activityWithImageSaved.id!!)
        verify(activityImageService).deleteActivityImage(
            activityWithImageSaved.id!!,
            activityWithImageSaved.insertDate!!
        )
    }

    private companion object {
        private val USER = createUser()

        private val organization = Organization(1L, "Autentia", emptyList())
        private val project = Project(1L, "Back-end developers", true, false, organization, emptyList())
        private val projectRole =
            ProjectRole(10, "Kotlin developer", RequireEvidence.NO, project, 0, true, false, TimeUnit.MINUTES)

        private val TODAY_NOON = LocalDateTime.of(LocalDate.now(), LocalTime.NOON)

        private const val notFoundActivityId = 1L

        private val activityWithoutImage = com.autentia.tnt.binnacle.core.domain.Activity.of(
            null,
            TimeInterval.of(
                LocalDateTime.of(LocalDate.now(), LocalTime.NOON),
                LocalDateTime.of(LocalDate.now(), LocalTime.NOON).plusMinutes(60)
            ),
            60,
            "Dummy description",
            projectRole.toDomain(),
            1L,
            false,
            1L,
            null,
            false,
            ApprovalState.NA
        )

        private val activityWithImage = com.autentia.tnt.binnacle.core.domain.Activity.of(
            null,
            TimeInterval.of(
                LocalDateTime.of(LocalDate.now(), LocalTime.NOON),
                LocalDateTime.of(LocalDate.now(), LocalTime.NOON).plusMinutes(120)
            ),
            120,
            "Description...",
            projectRole.toDomain(),
            1L,
            false,
            1L,
            null,
            true,
            ApprovalState.NA
        )

        private val image = "Base64 format..."
    }

}
