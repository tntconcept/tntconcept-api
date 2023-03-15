package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.entities.dto.ActivityDateDTO
import com.autentia.tnt.binnacle.exception.ActivityBeforeHiringDateException
import com.autentia.tnt.binnacle.exception.ActivityNotFoundException
import com.autentia.tnt.binnacle.exception.ActivityPeriodClosedException
import com.autentia.tnt.binnacle.exception.NoImageInActivityException
import com.autentia.tnt.binnacle.exception.OverlapsAnotherTimeException
import com.autentia.tnt.binnacle.exception.ProjectClosedException
import com.autentia.tnt.binnacle.exception.ProjectRoleNotFoundException
import com.autentia.tnt.binnacle.exception.UserPermissionException
import com.autentia.tnt.binnacle.usecases.ActivitiesBetweenDateUseCase
import com.autentia.tnt.binnacle.usecases.ActivitiesSummaryUseCase
import com.autentia.tnt.binnacle.usecases.ActivityCreationUseCase
import com.autentia.tnt.binnacle.usecases.ActivityDeletionUseCase
import com.autentia.tnt.binnacle.usecases.ActivityImageRetrievalUseCase
import com.autentia.tnt.binnacle.usecases.ActivityRetrievalByIdUseCase
import com.autentia.tnt.binnacle.usecases.ActivityUpdateUseCase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.BDDMockito.willDoNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month

internal class ActivityControllerTest {

    private val activitiesBetweenDateUseCase = mock<ActivitiesBetweenDateUseCase>()
    private val activityRetrievalUseCase = mock<ActivityRetrievalByIdUseCase>()
    private val activityCreationUseCase = mock<ActivityCreationUseCase>()
    private val activityUpdateUseCase = mock<ActivityUpdateUseCase>()
    private val activityDeletionUseCase = mock<ActivityDeletionUseCase>()
    private val activityImageRetrievalUseCase = mock<ActivityImageRetrievalUseCase>()
    private val activitiesSummaryUseCase = mock<ActivitiesSummaryUseCase>()

    private val activityController = ActivityController(
        activitiesBetweenDateUseCase,
        activityRetrievalUseCase,
        activityCreationUseCase,
        activityUpdateUseCase,
        activityDeletionUseCase,
        activityImageRetrievalUseCase,
        activitiesSummaryUseCase,
    )

    @Test
    fun `return all activities between the start and end date`() {
        //Given
        val startDate = LocalDate.of(2019, Month.DECEMBER, 30)
        val endDate = LocalDate.of(2020, Month.FEBRUARY, 2)

        val activitiesResponse = listOf(
            ActivityDateDTO(
                date = LocalDate.of(2019, Month.DECEMBER, 30),
                workedMinutes = 120,
                activities = listOf(
                    createActivityResponseDTO(1L, LocalDate.of(2019, Month.DECEMBER, 30).atStartOfDay(), LocalDate.of(2019, Month.DECEMBER, 30).atStartOfDay(), false),
                    createActivityResponseDTO(
                        2L,
                        LocalDate.of(2019, Month.DECEMBER, 30).atStartOfDay().plusHours(1),
                        LocalDate.of(2019, Month.DECEMBER, 30).atStartOfDay().plusHours(1),
                        false
                    ),
                )
            ),
            ActivityDateDTO(
                date = LocalDate.of(2020, Month.FEBRUARY, 2),
                workedMinutes = 120,
                activities = listOf(
                    createActivityResponseDTO(3L, LocalDate.of(2020, Month.FEBRUARY, 2).atStartOfDay(), LocalDate.of(2020, Month.FEBRUARY, 2).atStartOfDay(), false)
                )
            )
        )

        doReturn(activitiesResponse).whenever(activitiesBetweenDateUseCase).getActivities(startDate, endDate)

        //When
        val actualActivityResponse = activityController.get(startDate, endDate)

        //Then
        assertEquals(activitiesResponse, actualActivityResponse)
    }

    @Nested
    inner class GetActivity {
        @Test
        fun `return the activity`() {
            val activity =
                createActivityResponseDTO(ACTIVITY_ID, LocalDate.of(2020, Month.JULY, 2).atStartOfDay(), LocalDate.of(2020, Month.JULY, 2).atStartOfDay(), false)

            doReturn(activity).whenever(activityRetrievalUseCase).getActivityById(ACTIVITY_ID)

            val actualActivity = activityController.get(ACTIVITY_ID)

            assertEquals(activity, actualActivity)
        }

        @Test
        fun `throws UserPermissionException if the activity does not belong to logged user`() {
            // Given
            doThrow(UserPermissionException()).whenever(activityRetrievalUseCase).getActivityById(ACTIVITY_ID)

            // When
            val exception = assertThrows<UserPermissionException> {
                activityController.get(ACTIVITY_ID)
            }

            // Then
            assertThat(exception.message).isEqualTo("You don't have permission to access the resource")
        }
    }

    @Nested
    inner class GetActivityImage() {
        @Test
        fun `return the activity image`() {
            val base64 = "Base64 image..."
            doReturn(base64).whenever(activityImageRetrievalUseCase).getActivityImage(ACTIVITY_ID)

            val actualBase64 = activityController.getImage(ACTIVITY_ID)

            assertEquals(base64, actualBase64)
        }

        @Test
        fun `throws UserPermissionException when the activity does not belong to logged user`() {

            doThrow(UserPermissionException()).whenever(activityImageRetrievalUseCase).getActivityImage(ACTIVITY_ID)

            //When
            val exception = assertThrows<UserPermissionException> {
                activityController.getImage(ACTIVITY_ID)
            }

            //Then
            assertThat(exception.message).isEqualTo("You don't have permission to access the resource")
        }

        @Test
        fun `throws ActivityNotFoundException when activity can't be found`() {

            doThrow(ActivityNotFoundException(ACTIVITY_ID)).whenever(activityImageRetrievalUseCase).getActivityImage(
                ACTIVITY_ID
            )

            //When
            val exception = assertThrows<ActivityNotFoundException> {
                activityController.getImage(ACTIVITY_ID)
            }

            //Then
            assertThat(exception.id).isEqualTo(ACTIVITY_ID)
            assertThat(exception.message).isEqualTo("Activity (id: $ACTIVITY_ID) not found")
        }

        @Test
        fun `throws NoImageInActivityException when the activity does not have an image`() {

            doThrow(NoImageInActivityException(ACTIVITY_ID)).whenever(activityImageRetrievalUseCase).getActivityImage(
                ACTIVITY_ID
            )

            //When
            val exception = assertThrows<NoImageInActivityException> {
                activityController.getImage(ACTIVITY_ID)
            }

            //Then
            assertThat(exception.id).isEqualTo(ACTIVITY_ID)
            assertThat(exception.message).isEqualTo("Activity (id: $ACTIVITY_ID) not found")
        }

    }

    @Nested
    inner class CreateActivity() {
        @Test
        fun `create new activity when is valid`() {
            val start = LocalDate.of(2020, Month.JULY, 31).atTime(8, 45)
            val end = LocalDate.of(2020, Month.JULY, 31).atTime(8, 45)

            val newActivity = createActivityRequestBodyDTO(ACTIVITY_ID, start, end,1L, false)

            val expectedActivityResponseDTO = createActivityResponseDTO(ACTIVITY_ID, start, end,false)

            doReturn(expectedActivityResponseDTO).whenever(activityCreationUseCase).createActivity(newActivity)

            val actualActivityResponseDTO = activityController.post(newActivity)

            assertEquals(expectedActivityResponseDTO, actualActivityResponseDTO)
        }

//        @Test
//        fun `should create activity when description exceeds max length but it is truncated`() {
//            val startDate = LocalDate.of(2020, Month.JULY, 31).atTime(8, 45)
//            var description = "This description is not valid"
//            for (i in 1..2048) {
//                description += "A"
//            }
//
//            val newActivity = createActivityRequestBodyDTO(activityId, startDate, 1L, false)
//
//            val expectedActivityResponseDTO = createActivityResponseDTO(activityId, startDate, false)
//
//            given(activityCreationUseCase.createActivityByTruncatingDescription(newActivity)).willReturn(
//                expectedActivityResponseDTO
//            )
//
//            val actualActivityResponseDTO = activityController.createActivityByTruncatingDescription(newActivity)
//
//            assertEquals(expectedActivityResponseDTO, actualActivityResponseDTO)
//        }

        @Test
        fun `throws ActivityPeriodClosedException when activity period is closed`() {

            val newActivity = createActivityRequestBodyDTO(ACTIVITY_ID, LocalDateTime.now(), LocalDateTime.now(),1L, false)

            doThrow(ActivityPeriodClosedException()).whenever(activityCreationUseCase).createActivity(newActivity)

            //When
            val exception = assertThrows<ActivityPeriodClosedException> {
                activityController.post(newActivity)
            }

            //Then
            assertThat(exception.message).isEqualTo("The period of time of the activity is closed for modifications")

        }

        @Test
        fun `throws OverlapsAnotherTimeException when activity time overlaps another activity`() {
            //Given
            val newActivity = createActivityRequestBodyDTO(ACTIVITY_ID, LocalDateTime.now(), LocalDateTime.now(),1L, false)

            doThrow(OverlapsAnotherTimeException()).whenever(activityCreationUseCase).createActivity(newActivity)

            //When
            val exception = assertThrows<OverlapsAnotherTimeException> {
                activityController.post(newActivity)
            }

            //Then
            assertThat(exception.message).isEqualTo("There is already an activity in the indicated period of time")

        }

        @Test
        fun `throws ProjectRoleNotFoundException when the activity project role is not found in the database`() {
            val projectRoleId = 10L
            val newActivity = createActivityRequestBodyDTO(ACTIVITY_ID, LocalDateTime.now(), LocalDateTime.now(), projectRoleId, false)
            doThrow(ProjectRoleNotFoundException(projectRoleId))
                .whenever(activityCreationUseCase).createActivity(newActivity)

            val exception = assertThrows<ProjectRoleNotFoundException> {
                activityController.post(newActivity)
            }

            assertThat(exception.id).isEqualTo(projectRoleId)
            assertThat(exception.message).isEqualTo("Project role (id: $projectRoleId) not found")
        }

        @Test
        fun `throws ProjectClosedException when the activity project is closed`() {
            val newActivity = createActivityRequestBodyDTO(ACTIVITY_ID, LocalDateTime.now(), LocalDateTime.now(), 1L, false)

            doThrow(ProjectClosedException()).whenever(activityCreationUseCase).createActivity(newActivity)

            val exception = assertThrows<ProjectClosedException> {
                activityController.post(newActivity)
            }

            assertThat(exception.message).isEqualTo("The project is closed")
        }

        @Test
        fun `throws ActivityBeforeHiringDateException when the activity date is before user hiring date`() {
            val newActivity = createActivityRequestBodyDTO(ACTIVITY_ID, LocalDateTime.now(), LocalDateTime.now(), 1L, false)
            doThrow(ActivityBeforeHiringDateException()).whenever(activityCreationUseCase).createActivity(newActivity)

            val exception = assertThrows<ActivityBeforeHiringDateException> {
                activityController.post(newActivity)
            }

            assertThat(exception.message).isEqualTo("The activity start date is before user hiring date")
        }

    }

    @Nested
    inner class UpdateActivity() {

        private val activityToUpdate = createActivityRequestBodyDTO(ACTIVITY_ID, START, END,  PROJECT_ROLE_ID, false)

        @Test
        fun `update new activity when is valid`() {
            val expectedActivityResponseDTO = createActivityResponseDTO(ACTIVITY_ID, START, END,false)

            doReturn(expectedActivityResponseDTO).whenever(activityUpdateUseCase).updateActivity(activityToUpdate)

            val actualActivityResponseDTO = activityController.put(activityToUpdate)

            assertEquals(expectedActivityResponseDTO, actualActivityResponseDTO)

        }

        @Test
        fun `throws UserPermissionException when attempting to update an activity that does not belong to the logged user`() {
            doThrow(UserPermissionException()).whenever(activityUpdateUseCase).updateActivity(activityToUpdate)

            val exception = assertThrows<UserPermissionException> {
                activityController.put(activityToUpdate)
            }

            assertThat(exception.message).isEqualTo("You don't have permission to access the resource")
        }

        @Test
        fun `throws OverlapsAnotherTimeException when new activity time overlaps another existing activity`() {
            doThrow(OverlapsAnotherTimeException()).whenever(activityUpdateUseCase).updateActivity(activityToUpdate)

            val exception = assertThrows<OverlapsAnotherTimeException> {
                activityController.put(activityToUpdate)
            }

            assertThat(exception.message)
                .isEqualTo("There is already an activity in the indicated period of time")
        }

        @Test
        fun `throws ProjectClosedException when the activity project is closed`() {
            doThrow(ProjectClosedException()).whenever(activityUpdateUseCase).updateActivity(activityToUpdate)

            val exception = assertThrows<ProjectClosedException> {
                activityController.put(activityToUpdate)
            }

            assertThat(exception.message).isEqualTo("The project is closed")
        }

        @Test
        fun `throws ActivityPeriodClosedException when the activity period is closed`() {
            doThrow(ActivityPeriodClosedException()).whenever(activityUpdateUseCase).updateActivity(activityToUpdate)

            val exception = assertThrows<ActivityPeriodClosedException> {
                activityController.put(activityToUpdate)
            }

            assertThat(exception.message)
                .isEqualTo("The period of time of the activity is closed for modifications")
        }

        @Test
        fun `throws ProjectRoleNotFoundException when the activity project role is not found in the database`() {
            doThrow(ProjectRoleNotFoundException(PROJECT_ROLE_ID)).whenever(activityUpdateUseCase)
                .updateActivity(activityToUpdate)

            val exception = assertThrows<ProjectRoleNotFoundException> {
                activityController.put(activityToUpdate)
            }

            assertThat(exception.id).isEqualTo(PROJECT_ROLE_ID)
            assertThat(exception.message).isEqualTo("Project role (id: $PROJECT_ROLE_ID) not found")
        }

        @Test
        fun `throws ActivityNotFoundException when the activity to update is not found in the database`() {
            doThrow(ActivityNotFoundException(ACTIVITY_ID)).whenever(activityUpdateUseCase)
                .updateActivity(activityToUpdate)

            val exception = assertThrows<ActivityNotFoundException> {
                activityController.put(activityToUpdate)
            }

            assertThat(exception.id).isEqualTo(ACTIVITY_ID)
            assertThat(exception.message).isEqualTo("Activity (id: $ACTIVITY_ID) not found")
        }

        @Test
        fun `throws ActivityBeforeHiringDateException when the activity date is before user hiring date`() {
            doThrow(ActivityBeforeHiringDateException()).whenever(activityUpdateUseCase)
                .updateActivity(activityToUpdate)

            val exception = assertThrows<ActivityBeforeHiringDateException> {
                activityController.put(activityToUpdate)
            }

            assertThat(exception.message).isEqualTo("The activity start date is before user hiring date")
        }
    }

    @Nested
    inner class DeleteActivity() {
        @Test
        fun `return Ok when the activity has been deleted`() {

            willDoNothing().given(activityDeletionUseCase).deleteActivityById(ACTIVITY_ID)

            activityController.delete(ACTIVITY_ID)

            verify(activityDeletionUseCase, times(1)).deleteActivityById(ACTIVITY_ID)
        }

        @Test
        fun `throws UserPermissionException when attempting to delete an activity that does not belong to the logged user`() {
            doThrow(UserPermissionException()).whenever(activityDeletionUseCase).deleteActivityById(ACTIVITY_ID)

            val exception = assertThrows<UserPermissionException> {
                activityController.delete(ACTIVITY_ID)
            }

            assertThat(exception.message).isEqualTo("You don't have permission to access the resource")

        }

        @Test
        fun `throws ActivityNotFoundException when the activity to delete is not found in the database`() {
            doThrow(ActivityNotFoundException(ACTIVITY_ID)).whenever(activityDeletionUseCase).deleteActivityById(
                ACTIVITY_ID
            )

            val exception = assertThrows<ActivityNotFoundException> {
                activityController.delete(ACTIVITY_ID)
            }

            assertThat(exception.id).isEqualTo(ACTIVITY_ID)
            assertThat(exception.message).isEqualTo("Activity (id: $ACTIVITY_ID) not found")
        }

        @Test
        fun `throws ActivityPeriodClosedException when the activity period is closed`() {
            doThrow(ActivityPeriodClosedException()).whenever(activityDeletionUseCase).deleteActivityById(ACTIVITY_ID)

            val exception = assertThrows<ActivityPeriodClosedException> {
                activityController.delete(ACTIVITY_ID)
            }

            assertThat(exception.message)
                .isEqualTo("The period of time of the activity is closed for modifications")
        }

    }

    private companion object {
        private const val ACTIVITY_ID = 2L
        private const val PROJECT_ROLE_ID = 10L
        private val START = LocalDate.of(2020, Month.JULY, 31).atTime(8, 45)
        private val END = LocalDate.of(2020, Month.JULY, 31).atTime(8, 45)
    }

}
