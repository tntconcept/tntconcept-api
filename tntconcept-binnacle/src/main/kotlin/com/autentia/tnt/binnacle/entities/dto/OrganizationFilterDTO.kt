package com.autentia.tnt.binnacle.entities.dto

import com.autentia.tnt.binnacle.core.domain.OrganizationType

data class OrganizationFilterDTO (
    val type: List<OrganizationType>?,
    val imputable: Boolean?,
)