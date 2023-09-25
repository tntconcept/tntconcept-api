package com.autentia.tnt.binnacle.entities.dto

import com.autentia.tnt.binnacle.core.domain.OrganizationType

data class OrganizationFilterDTO (
    val types: List<OrganizationType>,
    val imputable: Boolean?,
)