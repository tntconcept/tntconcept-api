package com.autentia.tnt.binnacle.core.domain

data class Project(
    val id: Long,
    val name: String,
    val open: Boolean,
    val billable: Boolean,
    val organization: Organization
)