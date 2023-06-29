package com.autentia.tnt.api.binnacle.vacation

import com.autentia.tnt.binnacle.entities.dto.RequestVacationDTO
import io.micronaut.core.annotation.Introspected
import java.time.LocalDate
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
data class RequestVacation(
    val id: Long? = null,

    @field:NotNull
    val startDate: LocalDate,

    @field:NotNull
    val endDate: LocalDate,

    @field:Size(max = 1024, message = "Description must not exceed 1024 characters")
    var description: String? = null
) {
    fun toDto(): RequestVacationDTO = RequestVacationDTO(
        id,
        startDate,
        endDate,
        description,
    )
}