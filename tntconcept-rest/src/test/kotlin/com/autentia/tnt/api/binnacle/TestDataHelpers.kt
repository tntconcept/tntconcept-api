package com.autentia.tnt.api.binnacle

import com.autentia.tnt.binnacle.entities.Organization

internal fun createOrganization(id: Long = 1L) = Organization(
    id = id,
    name = "Dummy Organization",
    organizationTypeId = 1,
    projects = listOf()
)

