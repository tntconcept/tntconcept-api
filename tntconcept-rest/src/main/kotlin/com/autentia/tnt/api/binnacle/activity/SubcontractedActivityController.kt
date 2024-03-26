package com.autentia.tnt.api.binnacle.activity

import com.autentia.tnt.api.OpenApiTag
import com.autentia.tnt.binnacle.usecases.*
import io.micronaut.http.annotation.*
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.*
import javax.validation.Valid

@Controller("/api/subcontracted_activity")
@Validated
@Tag(name = OpenApiTag.ACTIVITY)
internal class SubcontractedActivityController (
        private val subcontractedActivityCreationUseCase: SubcontractedActivityCreationUseCase,
        private val subcontractedActivityUpdateUseCase: SubcontractedActivityUpdateUseCase,
        private val subcontractedActivityDeletionUseCase: SubcontractedActivityDeletionUseCase,

) {
    @Post
    @Operation(summary = "Creates a new subcontracted activity")
    internal fun post(
            @Body @Valid subcontractedActivityRequest: SubcontractedActivityRequest, @Parameter(hidden = true) locale: Locale,
    ): SubcontractedActivityResponse = SubcontractedActivityResponse.from(subcontractedActivityCreationUseCase.createSubcontractedActivity(subcontractedActivityRequest.toDto(), locale))


    @Put
    @Operation(summary = "Updates an existing subcontracted activity")
    internal fun put(
            @Valid @Body subcontractedActivityRequest: SubcontractedActivityRequest, @Parameter(hidden = true) locale: Locale,
    ): SubcontractedActivityResponse = SubcontractedActivityResponse.from(subcontractedActivityUpdateUseCase.updateSubcontractedActivity(subcontractedActivityRequest.toDto(), locale))

    @Delete("/{id}")
    @Operation(summary = "Deletes a subcontracted activity by its id.")
    internal fun delete(id: Long) = subcontractedActivityDeletionUseCase.deleteSubcontractedActivityById(id)

}