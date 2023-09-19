package com.autentia.tnt.api.binnacle.user

import com.autentia.tnt.binnacle.usecases.FindUserInfoUseCase
import com.autentia.tnt.binnacle.usecases.UsersRetrievalUseCase
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.http.annotation.Get
import io.swagger.v3.oas.annotations.Operation

@Controller("/api/user")
internal class UserController(
    private val findUserInfoUseCase: FindUserInfoUseCase,
    private val usersRetrievalUseCase: UsersRetrievalUseCase
) {
    @Get("{?userFilterRequest*}")
    @Operation(summary = "Retrieves the list of users with specified filters")
    internal fun get(userFilterRequest: UserFilterRequest): List<UserResponse> =
        usersRetrievalUseCase.getUsers(userFilterRequest.toDto()).map { UserResponse.from(it) }

    @Operation(summary = "Retrieves the logged user")
    @Get("/me")
    internal fun getUser(): UserInfoResponse =
        UserInfoResponse.from(findUserInfoUseCase.find())

    @Error
    internal fun onIllegalStateException(request: HttpRequest<*>, e: IllegalStateException) =
        HttpResponse.notFound<Any>()

}
