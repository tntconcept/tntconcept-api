package com.autentia.tnt.api.binnacle

import com.autentia.tnt.api.binnacle.request.activity.ActivityRequest
import com.autentia.tnt.api.binnacle.request.activity.ActivityRequestConverter
import com.autentia.tnt.binnacle.entities.dto.ActivityFilterDTO
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.exception.*
import com.autentia.tnt.binnacle.usecases.*
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.Operation
import java.time.LocalDate
import java.util.*
import javax.validation.Valid

@Controller("/api/activity")
@Validated
internal class ActivityController(
    private val activityByFilterUserCase: ActivitiesByFilterUseCase,
    private val activityRetrievalUseCase: ActivityRetrievalByIdUseCase,
    private val activityCreationUseCase: ActivityCreationUseCase,
    private val activityUpdateUseCase: ActivityUpdateUseCase,
    private val activityDeletionUseCase: ActivityDeletionUseCase,
    private val activityImageRetrievalUseCase: ActivityImageRetrievalUseCase,
    private val activitiesSummaryUseCase: ActivitiesSummaryUseCase,
    private val activityApprovalUseCase: ActivityApprovalUseCase,
    private val activityRequestConverter: ActivityRequestConverter
) {

    @Get("{?activityFilterDTO*}")
    @Operation(summary = "Gets activities with specified filters")
    internal fun get(activityFilterDTO: ActivityFilterDTO): List<ActivityResponseDTO> =
        activityByFilterUserCase.getActivities(activityFilterDTO)


    @Get("/{id}")
    @Operation(summary = "Gets an activity by its id.")
    internal fun get(id: Long): ActivityResponseDTO? = activityRetrievalUseCase.getActivityById(id)

    @Get("/{id}/image")
    @Operation(summary = "Retrieves an activity image by the activity id.")
    internal fun getImage(id: Long): String = activityImageRetrievalUseCase.getActivityImage(id)

    @Post
    @Operation(summary = "Creates a new activity.")
    internal fun post(@Body @Valid activityRequest: ActivityRequest, locale: Locale): ActivityResponseDTO =
        activityCreationUseCase.createActivity(activityRequestConverter.convertTo(activityRequest), locale)

    @Put
    @Operation(summary = "Updates an existing activity.")
    internal fun put(@Valid @Body activityRequest: ActivityRequest, locale: Locale): ActivityResponseDTO =
        activityUpdateUseCase.updateActivity(activityRequestConverter.convertTo(activityRequest), locale)


    @Delete("/{id}")
    @Operation(summary = "Deletes an activity by its id.")
    internal fun delete(id: Long) = activityDeletionUseCase.deleteActivityById(id)


    @Get("/summary")
    @Operation(summary = "Gets activities summary between two dates")
    internal fun summary(startDate: LocalDate, endDate: LocalDate) =
        activitiesSummaryUseCase.getActivitiesSummary(startDate, endDate)

    @Post("/{id}/approve")
    @Operation(summary = "Approve an existing activity by id.")
    internal fun approve(@Body id: Long, locale: Locale): ActivityResponseDTO =
        activityApprovalUseCase.approveActivity(id, locale)

    @Error
    internal fun onTimeIntervalException(request: HttpRequest<*>, e: TimeIntervalException) =
        HttpResponse.badRequest(ErrorResponse("INVALID_DATE_RANGE", e.message))

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
    internal fun onNoImageInActivityException(request: HttpRequest<*>, e: NoImageInActivityException) =
        HttpResponse.badRequest(ErrorResponse("No image", e.message))

    @Error
    internal fun onActivityAlreadyApproved(request: HttpRequest<*>, e: InvalidActivityApprovalStateException) =
        HttpResponse.status<HttpStatus>(HttpStatus.CONFLICT)
            .body(ErrorResponse("INVALID_ACTIVITY_APPROVAL_STATE", e.message))

}
