package com.autentia.tnt.api.binnacle.organization

import com.autentia.tnt.api.OpenApiTag.Companion.ORGANIZATION
import com.autentia.tnt.binnacle.usecases.OrganizationsByFilterUseCase
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag

@Controller("/api/organization")
@Tag(name = ORGANIZATION)
internal class OrganizationController(
    private val organizationsByFilterUseCase: OrganizationsByFilterUseCase
) {

    @Operation(summary = "Retrieves a list of organizations by filter")
    @Get("{?organizationFilterRequest*}")
    fun getAllOrganizations(organizationFilterRequest: OrganizationFilterRequest): List<OrganizationResponse> =
        organizationsByFilterUseCase.get(organizationFilterRequest.toDto()).map { OrganizationResponse.from(it) }



}