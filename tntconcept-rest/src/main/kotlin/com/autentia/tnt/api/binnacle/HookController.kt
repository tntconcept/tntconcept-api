package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.converters.ProjectResponseConverter
import com.autentia.tnt.binnacle.entities.dto.*
import com.autentia.tnt.binnacle.exception.ActivityBeforeHiringDateException
import com.autentia.tnt.binnacle.exception.ActivityPeriodClosedException
import com.autentia.tnt.binnacle.exception.MaxHoursPerRoleException
import com.autentia.tnt.binnacle.exception.NoImageInActivityException
import com.autentia.tnt.binnacle.exception.OverlapsAnotherTimeException
import com.autentia.tnt.binnacle.exception.ProjectClosedException
import com.autentia.tnt.binnacle.usecases.ActivitiesBetweenDateHookUseCase
import com.autentia.tnt.binnacle.usecases.ActivityCreationHookUseCase
import com.autentia.tnt.binnacle.usecases.ImputableOrganizationsUseCase
import com.autentia.tnt.binnacle.usecases.ImputableProjectsByOrganizationIdUseCase
import com.autentia.tnt.binnacle.usecases.ProjectRolesByProjectIdUseCase
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
    private val projectResponseConverter: ProjectResponseConverter
) {
    @Post("/activity")
    @Operation(summary = "Creates a new activity.")
    internal fun post(@Valid activityRequest: ActivityRequestBodyHookDTO): ActivityResponseDTO =
        activityCreationUseCase.createActivity(activityRequest)

    @Get("/activity")
    @Operation(summary = "Gets activities between two dates.")
    internal fun get(startDate: LocalDate, endDate: LocalDate, user: String): List<ActivityDateDTO> =
        activitiesBetweenDateUseCase.getActivities(startDate, endDate, user)

    @Operation(summary = "Retrieves a list of all organizations")
    @Get("/organization")
    fun getAllOrganizations(): List<OrganizationResponseDTO> =
        imputableOrganizationsUseCase.get()

    @Operation(summary = "Retrieves a list of imputable projects from an organization ID")
    @Get("/organization/{id}/project")
    fun getOrganizationsProjects(id: Long): List<ProjectResponseDTO> {
        val projectList = imputableProjectsByOrganizationIdUseCase.get(id)
        return projectList.map { projectResponseConverter.toProjectResponseDTO(it) }
    }

    @Operation(summary = "Retrieves a list of project roles from a project ID")
    @Get("/project/{id}/role")
    fun getProjectRolesByProjectId(id: Int): List<ProjectRoleDTO> {
        return projectRolesByProjectIdUseCase.get(id)
    }

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
}