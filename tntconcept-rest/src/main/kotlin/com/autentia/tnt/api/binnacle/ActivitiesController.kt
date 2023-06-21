package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.entities.dto.*
import com.autentia.tnt.binnacle.exception.*
import com.autentia.tnt.binnacle.usecases.*
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.Operation
import java.time.LocalDate
import jakarta.validation.Valid

@Deprecated("Use Activity controller instead")
@Controller("/api/activities")
@Validated
internal class ActivitiesController(
    private val activitiesBetweenDateUseCase: ActivitiesBetweenDateUseCaseOld,
    private val activityRetrievalUseCase: ActivitiesRetrievalByIdUseCase,
    private val activityCreationUseCase: ActivitiesCreationUseCase,
    private val activityUpdateUseCase: ActivitiesUpdateUseCase,
    private val activityDeletionUseCase: ActivitiesDeletionUseCase,
    private val activityImageRetrievalUseCase: ActivitiesImageRetrievalUseCase
) {

    @Get
    @Operation(summary = "Gets activities between two dates.")
    internal fun get(startDate: LocalDate, endDate: LocalDate): List<ActivityDateDTO> =
        activitiesBetweenDateUseCase.getActivities(startDate, endDate)

    @Get("/{id}")
    @Operation(summary = "Gets an activity by its id.")
    internal fun get(id: Long): ActivitiesResponseDTO? =
        activityRetrievalUseCase.getActivityById(id)

    @Get("/{id}/image")
    @Operation(summary = "Retrieves an activity image by the activity id.")
    internal fun getImage(id: Long): String =
        activityImageRetrievalUseCase.getActivityImage(id)

    @Post
    @Operation(summary = "Creates a new activity.")
    internal fun post(@Valid activityRequest: ActivitiesRequestBodyDTO): ActivitiesResponseDTO =
        activityCreationUseCase.createActivity(activityRequest)

    @Put
    @Operation(summary = "Updates an existing activity.")
    internal fun put(@Valid @Body activityRequest: ActivitiesRequestBodyDTO): ActivitiesResponseDTO =
        activityUpdateUseCase.updateActivity(activityRequest)

    @Delete("/{id}")
    @Operation(summary = "Deletes an activity by its id.")
    internal fun delete(id: Long) =
        activityDeletionUseCase.deleteActivityById(id)

    @Error
    internal fun onOverlapAnotherActivityTimeException(request: HttpRequest<*>, e: OverlapsAnotherTimeException) =
        HttpResponse.badRequest(ErrorResponse("ACTIVITY_TIME_OVERLAPS", e.message))

    @Error
    internal fun onReachedMaxImputableHoursForRole(request: HttpRequest<*>, e: MaxHoursPerRoleException) =
        HttpResponse.badRequest(
            ErrorResponseMaxHoursLimit(
                "MAX_REGISTRABLE_HOURS_LIMIT_EXCEEDED",
                e.message,
                ErrorValues(e.maxAllowedHours, e.remainingHours, e.year)
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
    internal fun onNoEvidenceInActivityException(request: HttpRequest<*>, e: NoEvidenceInActivityException) =
        HttpResponse.badRequest(ErrorResponse("No image", e.message))

}