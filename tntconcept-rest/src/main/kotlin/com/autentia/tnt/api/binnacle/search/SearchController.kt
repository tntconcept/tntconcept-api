package com.autentia.tnt.api.binnacle.search

import com.autentia.tnt.binnacle.usecases.SearchByRoleIdUseCase
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue

@Controller("/api/search")
internal class SearchController(
    private val searchUseCase: SearchByRoleIdUseCase
) {

    @Get
    fun searchBy(roleIds: List<Long>, @QueryValue year: Int?): SearchResponse =
        SearchResponse.from(searchUseCase.get(roleIds, year))

}
