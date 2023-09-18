package com.autentia.tnt.binnacle.core.domain

enum class OrganizationType {
    CLIENT, PROVIDER, PROSPECT
}
data class Organization(val id: Long, val name: String)