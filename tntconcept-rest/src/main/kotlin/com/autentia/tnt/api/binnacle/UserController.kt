package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.entities.dto.UserResponseDTO
import com.autentia.tnt.binnacle.usecases.FindByUserNameUseCase
import com.autentia.tnt.binnacle.usecases.UsersRetrievalUseCase
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.http.annotation.Get
import io.swagger.v3.oas.annotations.Operation

@Controller("/api/user")
internal class UserController(
    private val findByUserNameUseCase: FindByUserNameUseCase,
    private val usersRetrievalUseCase: UsersRetrievalUseCase
) {
    @Operation(summary = "Retrieves the list of active users")
    @Get
    internal fun get(): List<UserResponseDTO> = usersRetrievalUseCase.getAllActiveUsers()

    @Operation(summary = "Retrieves the logged user")
    @Get("/me")
    internal fun getUser(): UserResponse =
        UserResponse(findByUserNameUseCase.find())

    @Error
    internal fun onIllegalStateException(request: HttpRequest<*>, e: IllegalStateException) =
        HttpResponse.notFound<Any>()

}
