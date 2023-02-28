package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.usecases.FindByUserNameUseCase
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.swagger.v3.oas.annotations.Operation
import java.security.Principal

@Controller("/api/user")
internal class UserController(
    private val findByUserNameUseCase: FindByUserNameUseCase
) {

    @Operation(summary = "Retrieves a logged user")
    @Get
    internal fun getLoggedUser(): UserResponse =
        UserResponse(findByUserNameUseCase.find())

    @Operation(summary = "Retrieves secured data only for administrator roles")
    @Get("/secured")
    @Secured("ROLE_ADMINISTRADOR")
    internal fun getSecuredData(principal: Principal): String = "Only ADMIN roles can see this text"

    @Error
    internal fun onIllegalStateException(request: HttpRequest<*>, e: IllegalStateException) =
        HttpResponse.unauthorized<ErrorResponse>().body(ErrorResponse("UNAUTHORIZED", e.message))

}
