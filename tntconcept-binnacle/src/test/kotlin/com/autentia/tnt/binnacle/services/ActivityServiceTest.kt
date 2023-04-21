package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.converters.ActivityRequestBodyConverter
import com.autentia.tnt.binnacle.core.domain.ActivityRequestBody
import com.autentia.tnt.binnacle.core.domain.DateInterval
import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.entities.ProjectRole
import com.autentia.tnt.binnacle.entities.RequireEvidence
import com.autentia.tnt.binnacle.entities.TimeUnit
import com.autentia.tnt.binnacle.exception.ActivityAlreadyApprovedException
import com.autentia.tnt.binnacle.exception.ActivityNotFoundException
import com.autentia.tnt.binnacle.exception.ProjectRoleNotFoundException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.ProjectRoleRepository
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.mock
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
import java.util.Optional

internal class ActivityServiceTest {

    private val activityRepository = mock<ActivityRepository>()
    private val projectRoleRepository = mock<ProjectRoleRepository>()
    private val activityImageService = mock<ActivityImageService>()
    private val activityRequestBodyConverter = ActivityRequestBodyConverter()
    private val activityService = ActivityService(
        activityRepository,
        projectRoleRepository,
        activityImageService,
        activityRequestBodyConverter
    )

    init {
        whenever(projectRoleRepository.findById(projectRole.id)).thenReturn(projectRole)
    }

    private val activityWithImageToSave = activityRequestBodyConverter.mapActivityRequestBodyToActivity(
        activityWithImageRequest,
        projectRole,
        USER
    )
    private val activityWithoutImageToSave = activityRequestBodyConverter.mapActivityRequestBodyToActivity(
        activityWithoutImageRequest,
        projectRole,
        USER
    )

    private val activityWithImageSaved =
        activityWithImageToSave.copy(id = 101, insertDate = Date(), approvalState = ApprovalState.PENDING)

    private val activityWithoutImageSaved =
        activityWithoutImageToSave.copy(id = 100L, insertDate = Date(), approvalState = ApprovalState.PENDING)


    @Test
    fun `get activity by id`() {
        whenever(activityRepository.findById(activityWithoutImageSaved.id as Long)).thenReturn(
            activityWithoutImageSaved
        )

        val actual = activityService.getActivityById(activityWithoutImageSaved.id as Long)

        assertEquals(activityWithoutImageSaved, actual)
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
    fun `create activity`() {
        whenever(activityRepository.save(activityWithoutImageToSave)).thenReturn(activityWithoutImageSaved)

        val result = activityService.createActivity(activityWithoutImageRequest, USER)

        assertEquals(activityWithoutImageSaved, result)
        verifyNoInteractions(activityImageService)
    }

    @Test
    fun `create activity and store image file`() {
        whenever(activityRepository.save(activityWithImageToSave)).thenReturn(activityWithImageSaved)

        val result = activityService.createActivity(activityWithImageRequest, USER)

        assertEquals(activityWithImageSaved, result)
        verify(activityImageService).storeActivityImage(
            activityWithImageSaved.id!!,
            activityWithImageRequest.imageFile,
            activityWithImageSaved.insertDate!!
        )
    }

    @Test
    fun `update activity`() {
        val activityRequest = ActivityRequestBody(
            activityWithoutImageSaved.id,
            TODAY_NOON,
            TODAY_NOON.plusMinutes(120),
            120,
            "Description...",
            false,
            projectRole.id,
            false,
            null
        )

        whenever(activityRepository.findById(activityWithoutImageSaved.id!!)).thenReturn(activityWithoutImageSaved)

        val savedActivity = mock(Activity::class.java)

        val activityToSave = activityRequestBodyConverter.mapActivityRequestBodyToActivity(
            activityRequest,
            projectRole,
            USER,
            activityWithoutImageSaved.insertDate
        )
        whenever(activityRepository.update(activityToSave)).thenReturn(savedActivity)

        val result = activityService.updateActivity(activityRequest, USER)

        assertEquals(savedActivity, result)
        verifyNoInteractions(activityImageService)
    }

    @Test
    fun `update activity and update the stored image`() {
        val activityId = 90L
        val activityRequest = ActivityRequestBody(
            activityId,
            LocalDateTime.of(LocalDate.now(), LocalTime.NOON),
            LocalDateTime.of(LocalDate.now(), LocalTime.NOON).plusMinutes(120),
            120,
            "Description...",
            false,
            projectRole.id,
            true,
            "Base64 format...",
        )
        val oldActivityInsertDate = Date()
        val oldActivity = Activity(
            id = activityId,
            start = activityRequest.start,
            duration = 120,
            end = activityRequest.start.plusHours(2L),
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
            .storeActivityImage(activityRequest.id!!, activityRequest.imageFile, oldActivityInsertDate)

        val activityToReturn = mock(Activity::class.java)

        given(
            activityRepository.update(
                activityRequestBodyConverter.mapActivityRequestBodyToActivity(
                    activityRequest,
                    projectRole,
                    USER,
                    oldActivityInsertDate
                )
            )
        ).willReturn(activityToReturn)

        val result = activityService.updateActivity(activityRequest, USER)

        assertThat(result).isEqualTo(activityToReturn)
        verify(activityImageService).storeActivityImage(
            activityRequest.id!!,
            activityRequest.imageFile,
            oldActivityInsertDate
        )
    }

    @Test
    fun `update activity and delete the stored image`() {
        val activityId = 90L
        val activityRequest = ActivityRequestBody(
            activityId,
            LocalDateTime.of(LocalDate.now(), LocalTime.NOON),
            LocalDateTime.of(LocalDate.now(), LocalTime.NOON).plusMinutes(120),
            120,
            "Description...",
            false,
            projectRole.id,
            false,
            null,
        )

        val oldActivityInsertDate = Date()
        val oldActivity = Activity(
            id = activityId,
            start = activityRequest.start,
            duration = 120,
            end = activityRequest.start.plusHours(2L),
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

        val savedActivity = mock<Activity>()

        given(
            activityRepository.update(
                activityRequestBodyConverter.mapActivityRequestBodyToActivity(
                    activityRequest,
                    projectRole,
                    USER,
                    oldActivityInsertDate
                )
            )
        ).willReturn(savedActivity)

        val result = activityService.updateActivity(activityRequest, USER)

        assertThat(result).isEqualTo(savedActivity)
        verify(activityImageService).deleteActivityImage(activityId, oldActivityInsertDate)
    }

    @Test
    fun `approve activity by id`(){
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
        assertThrows<ActivityAlreadyApprovedException> {
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
        private val projectRole = ProjectRole(10, "Kotlin developer", RequireEvidence.NO, project, 0, true, false, TimeUnit.MINUTES)

        private val TODAY_NOON = LocalDateTime.of(LocalDate.now(), LocalTime.NOON)

        private const val notFoundActivityId = 1L

        private val activityWithoutImageRequest = ActivityRequestBody(
            null,
            LocalDateTime.of(LocalDate.now(), LocalTime.NOON),
            LocalDateTime.of(LocalDate.now(), LocalTime.NOON).plusMinutes(60),
            60,
            "Dummy description",
            false,
            projectRole.id,
            false,
            null,
        )

        private val activityWithImageRequest = ActivityRequestBody(
            null,
            LocalDateTime.of(LocalDate.now(), LocalTime.NOON),
            LocalDateTime.of(LocalDate.now(), LocalTime.NOON).plusMinutes(120),
            120,
            "Description...",
            false,
            projectRole.id,
            true,
            "Base64 format...",
        )
    }

}
