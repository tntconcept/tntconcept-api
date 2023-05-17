package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.exception.ResourceNotFoundException
import com.autentia.tnt.binnacle.exception.UserPermissionException
import com.autentia.tnt.binnacle.utils.BinnacleApiIllegalArgumentException
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import jakarta.validation.ConstraintViolationException

@Controller("/")
internal class RestExceptionHandler {

    @Error(global = true)
    internal fun onUserPermissionException(request: HttpRequest<*>, e: UserPermissionException) =
        HttpResponse.notFound(ErrorResponse("RESOURCE_NOT_FOUND", e.message))

    @Error(global = true)
    internal fun onConstraintValidationException(
        request: HttpRequest<*>,
        e: ConstraintViolationException
    ): HttpResponse<ValidationErrorResponse> {
        val error = ValidationErrorResponse()
        for (violation in e.constraintViolations) {
            error.validationErrors.add(ValidationError(violation.propertyPath.toString(), violation.message))
        }

        return HttpResponse.badRequest<ValidationErrorResponse>().body(error)
    }

    @Error(global = true)
    internal fun onBinnacleIllegalArgumentException(request: HttpRequest<*>, e: BinnacleApiIllegalArgumentException) =
        HttpResponse.badRequest<ErrorResponse>().body(ErrorResponse(e.code, e.message))

    @Error(global = true)
    internal fun onResourceNotFoundException(request: HttpRequest<*>, e: ResourceNotFoundException) =
        HttpResponse.notFound<ErrorResponse>().body(ErrorResponse(e.code, e.message))

    @Error(global = true)
    internal fun onIllegalArgumentException(request: HttpRequest<*>, e: IllegalArgumentException) =
        HttpResponse.badRequest<ErrorResponse>().body(ErrorResponse("ILLEGAL_ARGUMENT", e.message))

    @Error(global = true)
    internal fun onIllegalStateException(request: HttpRequest<*>, e: IllegalStateException) =
        HttpResponse.serverError<ErrorResponse>().body(ErrorResponse("ILLEGAL_STATE", e.message))

}
