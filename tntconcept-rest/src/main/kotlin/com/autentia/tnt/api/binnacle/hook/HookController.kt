package com.autentia.tnt.api.binnacle.hook

import com.autentia.tnt.api.binnacle.ErrorResponse
import com.autentia.tnt.api.binnacle.ErrorResponseMaxTimeLimit
import com.autentia.tnt.api.binnacle.ErrorValues
import com.autentia.tnt.api.binnacle.activity.ActivityResponse
import com.autentia.tnt.api.binnacle.organization.OrganizationResponse
import com.autentia.tnt.api.binnacle.project.ProjectResponse
import com.autentia.tnt.api.binnacle.projectrole.ProjectRoleResponse
import com.autentia.tnt.binnacle.entities.dto.*
import com.autentia.tnt.binnacle.exception.*
import com.autentia.tnt.binnacle.usecases.*
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.Operation
import java.time.LocalDate
import javax.validation.Valid

@Controller("/api-hook")
@Validated
internal class HookController(
    private val activityCreationUseCase: ActivityCreationHookUseCase,
    private val activitiesBetweenDateUseCase: ActivitiesBetweenDateHookUseCase,
    private val imputableOrganizationsUseCase: ImputableOrganizationsUseCase,
    private val imputableProjectsByOrganizationIdUseCase: ImputableProjectsByOrganizationIdUseCase,
    private val projectRolesByProjectIdUseCase: ProjectRolesByProjectIdUseCase,
) {
    @Post("/activity")
    @Operation(summary = "Creates a new activity.")
    internal fun post(@Valid activityRequest: ActivityRequest): ActivityResponse =
        ActivityResponse.from(activityCreationUseCase.createActivity(activityRequest.toDto()))

    @Get("/activity")
    @Operation(summary = "Gets activities between two dates.")
    internal fun get(startDate: LocalDate, endDate: LocalDate, user: String): List<ActivityDateResponse> =
        activitiesBetweenDateUseCase.getActivities(startDate, endDate, user).map { ActivityDateResponse.from(it) }

    @Operation(summary = "Retrieves a list of all organizations")
    @Get("/organization")
    fun getAllOrganizations(): List<OrganizationResponse> =
        imputableOrganizationsUseCase.get().map { OrganizationResponse.from(it) }

    @Operation(summary = "Retrieves a list of imputable projects from an organization ID")
    @Get("/organization/{id}/project")
    fun getOrganizationsProjects(id: Long): List<ProjectResponse> {
        return imputableProjectsByOrganizationIdUseCase.get(id).map { ProjectResponse.from(it) }
    }

    @Operation(summary = "Retrieves a list of project roles from a project ID")
    @Get("/project/{id}/role")
    fun getProjectRolesByProjectId(id: Int): List<ProjectRoleResponse> {
        return projectRolesByProjectIdUseCase.get(id).map { ProjectRoleResponse.from(it) }
    }

    @Error
    internal fun onOverlapAnotherActivityTimeException(request: HttpRequest<*>, e: OverlapsAnotherTimeException) =
        HttpResponse.badRequest(ErrorResponse("ACTIVITY_TIME_OVERLAPS", e.message))

    @Error
    internal fun onReachedMaxImputableHoursForRole(request: HttpRequest<*>, e: MaxTimePerRoleException) =
        HttpResponse.badRequest(
            ErrorResponseMaxTimeLimit(
                "MAX_REGISTRABLE_HOURS_LIMIT_EXCEEDED",
                e.message,
                ErrorValues(e.maxAllowedTime, e.remainingTime, e.timeUnit, e.year)
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