package com.autentia.tnt.api.binnacle.activity

import com.autentia.tnt.api.OpenApiTag
import com.autentia.tnt.api.binnacle.ErrorResponse
import com.autentia.tnt.binnacle.exception.*
import com.autentia.tnt.binnacle.usecases.*
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.*
import javax.validation.Valid

@Controller("/api/subcontracted_activity")
@Validated
@Tag(name = OpenApiTag.ACTIVITY)
internal class SubcontractedActivityController (
        private val subcontractedActivityRetrievalByIdUseCase: SubcontractedActivityRetrievalByIdUseCase,
        private val subcontractedActivitiesByFilterUseCase: SubcontractedActivitiesByFilterUseCase,
        private val subcontractedActivityCreationUseCase: SubcontractedActivityCreationUseCase,
        private val subcontractedActivityUpdateUseCase: SubcontractedActivityUpdateUseCase,
        private val subcontractedActivityDeletionUseCase: SubcontractedActivityDeletionUseCase,

) {

    @Get("{?activityFilterRequest*}")
    @Operation(summary = "Gets activities with specified filters")
    internal fun get(activityFilterRequest: SubcontractedActivityFilterRequest): List<SubcontractedActivityResponse> =
        subcontractedActivitiesByFilterUseCase.getActivities(activityFilterRequest.toDto()).map { SubcontractedActivityResponse.from(it) }

    @Get("/{id}")
    @Operation(summary = "Gets an activity by its id.")
    internal fun get(id: Long): SubcontractedActivityResponse? =
        SubcontractedActivityResponse.from(subcontractedActivityRetrievalByIdUseCase.getActivityById(id))


    @Post
    @Operation(summary = "Creates a new subcontracted activity")
    internal fun post(
            @Body @Valid subcontractedActivityRequest: SubcontractedActivityRequest, @Parameter(hidden = true) locale: Locale,
    ): SubcontractedActivityResponse = SubcontractedActivityResponse.from(subcontractedActivityCreationUseCase.createSubcontractedActivity(subcontractedActivityRequest.toDto(), locale))


    @Put
    @Operation(summary = "Updates an existing subcontracted activity")
    internal fun put(
            @Valid @Body subcontractedActivityRequest: SubcontractedActivityRequest, @Parameter(hidden = true) locale: Locale,
    ): SubcontractedActivityResponse = SubcontractedActivityResponse.from(subcontractedActivityUpdateUseCase.updateSubcontractedActivity(subcontractedActivityRequest.toDto(), locale))

    @Delete("/{id}")
    @Operation(summary = "Deletes a subcontracted activity by its id.")
    internal fun delete(id: Long) = subcontractedActivityDeletionUseCase.deleteSubcontractedActivityById(id)

    @Error
    internal fun onNoEvidenceInActivityException(request: HttpRequest<*>, e: NoEvidenceInActivityException) =
        HttpResponse.badRequest(ErrorResponse("No evidence", e.message))

    @Error
    internal fun onProjectClosedException(request: HttpRequest<*>, e: ProjectClosedException) =
        HttpResponse.badRequest(ErrorResponse("CLOSED_PROJECT", e.message))

    @Error
    internal fun onActivityPeriodClosedException(request: HttpRequest<*>, e: ActivityPeriodClosedException) =
        HttpResponse.badRequest(ErrorResponse("ACTIVITY_PERIOD_CLOSED", e.message))

    @Error
    internal fun onActivityBeforeProjectCreationDate(request: HttpRequest<*>, e: ActivityBeforeProjectCreationDateException) =
        HttpResponse.badRequest(ErrorResponse("ACTIVITY_BEFORE_PROJECT_CREATION_DATE", e.message))

    @Error
    internal fun onProjectBlockedException(request: HttpRequest<*>, e: ProjectBlockedException) =
        HttpResponse.badRequest(
            ErrorResponseBlockedProject(
                "BLOCKED_PROJECT",
                e.message,
                ErrorResponseBlockedProject.ErrorValues(e.blockedDate)
            )
        )
    @Error
    internal fun onOverlapAnotherActivityTimeException(request: HttpRequest<*>, e: OverlapsAnotherTimeException) =
        HttpResponse.badRequest(ErrorResponse("ACTIVITY_TIME_OVERLAPS", e.message))

}