package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.dto.ActivityDateDTO
import com.autentia.tnt.binnacle.entities.dto.ActivityRequestBodyDTO
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.exception.ActivityBeforeHiringDateException
import com.autentia.tnt.binnacle.exception.ActivityPeriodClosedException
import com.autentia.tnt.binnacle.exception.MaxHoursPerRoleException
import com.autentia.tnt.binnacle.exception.NoImageInActivityException
import com.autentia.tnt.binnacle.exception.OverlapsAnotherTimeException
import com.autentia.tnt.binnacle.exception.ProjectClosedException
import com.autentia.tnt.binnacle.usecases.*
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import com.autentia.tnt.binnacle.exception.*
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Error
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.Operation
import java.time.LocalDate
import java.util.*
import javax.validation.Valid

@Controller("/api/activity")
@Validated
internal class ActivityController(
    private val activitiesBetweenDateUseCase: ActivitiesBetweenDateUseCase,
    private val activityRetrievalUseCase: ActivityRetrievalByIdUseCase,
    private val activityCreationUseCase: ActivityCreationUseCase,
    private val activityUpdateUseCase: ActivityUpdateUseCase,
    private val activityDeletionUseCase: ActivityDeletionUseCase,
    private val activityImageRetrievalUseCase: ActivityImageRetrievalUseCase,
    private val activityApprovalUseCase: ActivityApprovalUseCase
) {

    @Get
    @Operation(summary = "Gets activities between two dates.")
    internal fun get(start: Optional<LocalDate>, end: Optional<LocalDate>, approvalState: Optional<ApprovalState>): List<ActivityDateDTO> {
        return activitiesBetweenDateUseCase.getActivities(start, end, approvalState)
    }

    @Get("/{id}")
    @Operation(summary = "Gets an activity by its id.")
    internal fun get(id: Long): ActivityResponseDTO? =
        activityRetrievalUseCase.getActivityById(id)

    @Get("/{id}/image")
    @Operation(summary = "Retrieves an activity image by the activity id.")
    internal fun getImage(id: Long): String =
        activityImageRetrievalUseCase.getActivityImage(id)

    @Post
    @Operation(summary = "Creates a new activity.")
    internal fun post(@Valid activityRequest: ActivityRequestBodyDTO): ActivityResponseDTO =
        activityCreationUseCase.createActivity(activityRequest)

    @Put
    @Operation(summary = "Updates an existing activity.")
    internal fun put(@Valid @Body activityRequest: ActivityRequestBodyDTO): ActivityResponseDTO =
        activityUpdateUseCase.updateActivity(activityRequest)

    @Delete("/{id}")
    @Operation(summary = "Deletes an activity by its id.")
    internal fun delete(id: Long) =
        activityDeletionUseCase.deleteActivityById(id)

    @Post("/{id}/approve")
    @Operation(summary = "Approve an existing activity by id.")
    internal fun approve(id: Long): ActivityResponseDTO =
        activityApprovalUseCase.approveActivity(id)

    @Error
    internal fun onOverlapAnotherActivityTimeException(request: HttpRequest<*>, e: OverlapsAnotherTimeException) =
        HttpResponse.badRequest(ErrorResponse("ACTIVITY_TIME_OVERLAPS", e.message))

    @Error
    internal fun onReachedMaxImputableHoursForRole(request: HttpRequest<*>, e: MaxHoursPerRoleException) =
        HttpResponse.badRequest(
            ErrorResponseMaxHoursLimit(
                "MAX_REGISTRABLE_HOURS_LIMIT_EXCEEDED",
                e.message,
                ErrorValues(e.maxAllowedHours, e.remainingHours)
            )
        )

    @Error
    internal fun onProjectClosedException(request: HttpRequest<*>, e: ProjectClosedException) =
        HttpResponse.badRequest(ErrorResponse("CLOSED_PROJECT", e.message))

    @Error
    internal fun onActivityPeriodClosedException(request: HttpRequest<*>, e: ActivityPeriodClosedException) =
        HttpResponse.badRequest(ErrorResponse("ACTIVITY_PERIOD_CLOSED", e.message))

    @Error
    internal fun onActivityBeforeHiringDateException(request: HttpRequest<*>, e: ActivityBeforeHiringDateException) =
        HttpResponse.badRequest(ErrorResponse("ACTIVITY_BEFORE_HIRING_DATE", e.message))

    @Error
    internal fun onNoImageInActivityException(request: HttpRequest<*>, e: NoImageInActivityException) =
        HttpResponse.badRequest(ErrorResponse("No image", e.message))

    @Error
    internal fun onActivityAlreadyApproved(request: HttpRequest<*>, e: ActivityAlreadyApprovedException) =
        HttpResponse.status<HttpStatus>(HttpStatus.CONFLICT, e.message)

}
