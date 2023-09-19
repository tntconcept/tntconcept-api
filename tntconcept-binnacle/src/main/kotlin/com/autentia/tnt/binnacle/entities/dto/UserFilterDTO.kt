package com.autentia.tnt.binnacle.entities.dto

import io.micronaut.core.annotation.Introspected

@Introspected
data class UserFilterDTO(
    val ids: List<Long>? = null,
    val active: Boolean? = null,
    val filter: String? = null,
    val limit: Int? = null,
)