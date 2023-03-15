package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.usecases.FindByUserNameUseCase
import com.autentia.tnt.security.application.SecurityFindQry
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.http.annotation.Get
import io.swagger.v3.oas.annotations.Operation

@Controller("/api/user")
internal class UserController(
    private val findByUserNameUseCase: FindByUserNameUseCase,
    private val securityFindQry: SecurityFindQry
) {

    @Operation(summary = "Retrieves the logged user")
    @Get("/me")
    internal fun getUser(): UserResponse =
        UserResponse(findByUserNameUseCase.find())

    @Operation(summary = "Retrieves the security info of the logged user")
    @Get("/me/security")
    internal fun getUserSecurity(): UserSecurityResponse? {
        val subject = securityFindQry.find()
        return if (subject != null) UserSecurityResponse(subject) else null
    }

    @Error
    internal fun onIllegalStateException(request: HttpRequest<*>, e: IllegalStateException) =
        HttpResponse.notFound<Any>()

}
