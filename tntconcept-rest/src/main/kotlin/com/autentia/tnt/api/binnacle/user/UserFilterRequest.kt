package com.autentia.tnt.api.binnacle.user

import com.autentia.tnt.binnacle.entities.dto.UserFilterDTO
import io.micronaut.core.annotation.Introspected

@Introspected
data class UserFilterRequest(
    val ids: String? = null,
    val active: Boolean? = null,
    val nameLike: String? = null,
    val limit: Int? = null,
) {
    fun toDto(): UserFilterDTO =
        UserFilterDTO(
            ids?.split(",".trim())?.map { it.toLong() },
            active,
            nameLike,
            limit
        )
}
