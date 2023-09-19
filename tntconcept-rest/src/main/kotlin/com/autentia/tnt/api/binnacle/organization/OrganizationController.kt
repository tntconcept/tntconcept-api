package com.autentia.tnt.api.binnacle.organization

import com.autentia.tnt.binnacle.usecases.OrganizationsByFilterUseCase
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.swagger.v3.oas.annotations.Operation

@Controller("/api/organization")
internal class OrganizationController(
    private val organizationsByFilterUseCase: OrganizationsByFilterUseCase
) {

    @Operation(summary = "Retrieves a list of all organizations")
    @Get("{?organizationFilterRequest*}")
    fun getAllOrganizations(organizationFilterRequest: OrganizationFilterRequest): List<OrganizationResponse> =
        organizationsByFilterUseCase.get(organizationFilterRequest.toDto()).map { OrganizationResponse.from(it) }



}