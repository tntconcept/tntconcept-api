package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.exception.ActivityNotFoundException
import com.autentia.tnt.binnacle.services.ActivityService
import com.autentia.tnt.binnacle.services.UserService
import com.autentia.tnt.binnacle.validators.ActivityValidator
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class ActivityDeletionUseCaseTest {

    private val user = createUser()
    private val activityService = mock<ActivityService>()
    private val activityValidator = mock<ActivityValidator>()
    private val userService = mock<UserService>()

    private val useCase = ActivityDeletionUseCase(activityService, activityValidator)

    @Test
    fun `call the service to delete the activity`() {
        doReturn(user).whenever(userService).getAuthenticatedUser()
        useCase.deleteActivityById(1L)

        verify(activityService).deleteActivityById(1L)
    }

    @Test
    fun `throw not found exception from the validator`() {
        doReturn(user).whenever(userService).getAuthenticatedUser()
        doThrow(ActivityNotFoundException(1L)).whenever(activityValidator).checkActivityIsValidForDeletion(1L)

        assertThrows<ActivityNotFoundException> {
            useCase.deleteActivityById(1L)
        }

    }

}
