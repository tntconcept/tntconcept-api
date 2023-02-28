package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.entities.dto.SearchResponseDTO
import com.autentia.tnt.binnacle.usecases.SearchByRoleIdUseCase
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/api/search")
internal class SearchController(
    private val searchUseCase: SearchByRoleIdUseCase
) {

    @Get
    fun searchBy(roleIds: List<Long>): SearchResponseDTO {

        return searchUseCase.getDescriptions(roleIds)
    }
}
