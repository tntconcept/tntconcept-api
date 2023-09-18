package com.autentia.tnt.api.binnacle.user

import com.autentia.tnt.binnacle.usecases.FindUserInfoUseCase
import com.autentia.tnt.binnacle.usecases.UsersRetrievalUseCase
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.swagger.v3.oas.annotations.Operation

@Controller("/api/user")
internal class UserController(
    private val findUserInfoUseCase: FindUserInfoUseCase,
    private val usersRetrievalUseCase: UsersRetrievalUseCase
) {
    @Operation(summary = "Retrieves the list of active users")
    @Get
    internal fun get(@QueryValue ids: List<Long>?, @QueryValue active: Boolean?): List<UserResponse> =
        usersRetrievalUseCase.getUsers(ids, active).map { UserResponse.from(it) }

    @Operation(summary = "Retrieves the logged user")
    @Get("/me")
    internal fun getUser(): UserInfoResponse =
        UserInfoResponse.from(findUserInfoUseCase.find())

    @Error
    internal fun onIllegalStateException(request: HttpRequest<*>, e: IllegalStateException) =
        HttpResponse.notFound<Any>()

}
