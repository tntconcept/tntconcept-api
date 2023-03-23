package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.usecases.FindByUserNameUseCase
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.http.annotation.Get
import io.swagger.v3.oas.annotations.Operation

@Controller("/api/user")
internal class UserController(
    private val findByUserNameUseCase: FindByUserNameUseCase
) {

    @Operation(summary = "Retrieves the logged user")
    @Get("/me")
    internal fun getUser(): UserResponse =
        UserResponse(findByUserNameUseCase.find())

    @Error
    internal fun onIllegalStateException(request: HttpRequest<*>, e: IllegalStateException) =
        HttpResponse.notFound<Any>()

}
